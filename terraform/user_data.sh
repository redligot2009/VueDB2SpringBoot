#!/bin/bash

# User data script for EC2 instance setup
set -e

# Update system
apt-get update
apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
usermod -aG docker ubuntu

# Install Docker Compose v2 (plugin)
mkdir -p /usr/local/lib/docker/cli-plugins
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# Also install legacy docker-compose for compatibility
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install Nginx (for reverse proxy and SSL termination)
apt-get install -y nginx certbot python3-certbot-nginx

# Create application directory
mkdir -p /opt/${project_name}
cd /opt/${project_name}

# Create environment file for production
cat > .env << EOF
# Database Configuration
DB_URL=jdbc:db2://db2:50000/PHOTODB
DB_USER=db2inst1
DB_PASS=passw0rd

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-that-is-at-least-64-characters-long-for-hs512-algorithm
JWT_EXPIRATION=86400000

# Frontend Configuration
VITE_API_URL=https://${domain_name}/api
EOF

# Create production docker-compose file
cat > docker-compose.prod.yml << 'EOF'
services:
  db2:
    image: icr.io/db2_community/db2:11.5.8.0
    container_name: db2
    privileged: true
    environment:
      - LICENSE=accept
      - DB2INSTANCE=db2inst1
      - DB2INST1_PASSWORD=passw0rd
      - DBNAME=PHOTODB
      - BLU=false
      - ENABLE_ORACLE_COMPATIBILITY=false
      - UPDATEAVAIL=NO
      - TO_CREATE_SAMPLEDB=false
      - REPODB=false
      - IS_OSXFS=false
      - PERSISTENT_HOME=true
      - HADR_ENABLED=false
    volumes:
      - db2_data:/database
    restart: unless-stopped
    shm_size: 1g
    networks:
      - app-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: backend
    depends_on:
      - db2
    environment:
      - SPRING_PROFILES_ACTIVE=db2
      - DB_URL=jdbc:db2://db2:50000/PHOTODB
      - DB_USER=db2inst1
      - DB_PASS=passw0rd
      - JWT_SECRET=your-super-secure-jwt-secret-key-that-is-at-least-64-characters-long-for-hs512-algorithm
      - JWT_EXPIRATION=86400000
    restart: unless-stopped
    networks:
      - app-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: frontend
    depends_on:
      - backend
    environment:
      - VITE_API_URL=https://${domain_name}/api
    restart: unless-stopped
    networks:
      - app-network

volumes:
  db2_data:
    driver: local

networks:
  app-network:
    driver: bridge
EOF

# Create Nginx configuration
cat > /etc/nginx/sites-available/${project_name} << 'EOF'
server {
    listen 80;
    server_name ${domain_name};

    # Redirect all HTTP requests to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name ${domain_name};

    # SSL configuration will be added by certbot
    ssl_certificate /etc/letsencrypt/live/${domain_name}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${domain_name}/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Health check
    location /health {
        proxy_pass http://localhost:8080/health;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# Enable the site
ln -sf /etc/nginx/sites-available/${project_name} /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test Nginx configuration
nginx -t

# Create deployment script
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "Starting deployment..."

# Pull latest changes
git pull origin main

# Build and start services
docker compose -f docker-compose.prod.yml down || docker-compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml build --no-cache || docker-compose -f docker-compose.prod.yml build --no-cache
docker compose -f docker-compose.prod.yml up -d || docker-compose -f docker-compose.prod.yml up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 30

# Check if services are running
docker-compose -f docker-compose.prod.yml ps

echo "Deployment completed!"
EOF

chmod +x deploy.sh

# Create systemd service for auto-start
cat > /etc/systemd/system/${project_name}.service << EOF
[Unit]
Description=${project_name} Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/${project_name}
ExecStart=/usr/bin/docker compose -f docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

# Enable the service
systemctl enable ${project_name}.service

# Start Nginx
systemctl start nginx
systemctl enable nginx

# Create log rotation for Docker containers
cat > /etc/logrotate.d/docker-containers << 'EOF'
/var/lib/docker/containers/*/*.log {
    rotate 7
    daily
    compress
    size=1M
    missingok
    delaycompress
    copytruncate
}
EOF

echo "User data script completed successfully!"
