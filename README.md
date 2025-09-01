# VueDB2SpringBoot - Photo Gallery Application

A full-stack photo gallery application with Spring Boot backend and Vue.js frontend, featuring JWT authentication, photo management, advanced viewing capabilities, and complete CI/CD deployment to AWS.

## 🚀 Features

### 🔐 Authentication & User Management
- **JWT-based authentication** with secure token management
- **User registration and login** with password encryption
- **Profile management** - update username, email, and password
- **Session persistence** across browser refreshes
- **Route protection** - authenticated routes with automatic redirects

### 📸 Photo Management
- **Photo upload** with drag-and-drop support
- **Bulk upload** - upload multiple photos at once
- **Photo editing** - update titles and descriptions
- **Bulk deletion** - select and delete multiple photos
- **Photo metadata** - title, description, file size, upload date
- **Responsive gallery** with grid layout

### 🗂️ Gallery Management
- **Gallery organization** - create and manage multiple galleries
- **Photo organization** - move photos between galleries
- **Gallery filtering** - view photos by specific gallery or unorganized photos
- **Gallery editing** - update gallery names and descriptions
- **Gallery deletion** - remove galleries with photo management options
- **Gallery previews** - see first 4 photos in gallery overview
- **Bulk operations** - select and move multiple photos between galleries

📖 **[Detailed Gallery Features Documentation](GALLERY_FEATURE.md)**

### 🖼️ Advanced Photo Viewing
- **Full-screen photo viewer** with dark theme
- **Zoom functionality** - 50% to 300% zoom range
- **Mouse wheel zoom** for smooth zooming
- **Pan functionality** - drag to move around zoomed images
- **Bounded panning** - prevents images from going off-screen
- **Photo navigation** - browse through gallery photos
- **Keyboard shortcuts** - arrow keys, zoom controls, escape to exit

### 📱 User Interface
- **Responsive design** - works on desktop, tablet, and mobile
- **Modern UI** with smooth animations and hover effects
- **Pagination** - configurable page sizes (5, 10, 20, 50, 100)
- **Sticky pagination** - follows scroll for easy navigation
- **Loading states** and error handling
- **Modal dialogs** for confirmations and bulk operations

## 🏗️ Architecture

- **Backend**: Spring Boot REST API with JWT authentication
- **Frontend**: Vue.js 3 with TypeScript, Pinia state management
- **Database**: IBM DB2 Community Edition
- **Authentication**: JWT tokens with Spring Security
- **Containerization**: Docker with Docker Compose
- **Deployment**: AWS EC2 with Terraform, Cloudflare DNS, SSL

## 🚀 Quick Start

### Prerequisites
- Docker Desktop with at least 4GB RAM and 2 CPUs allocated
- Docker Compose

### Local Development with Docker

1. **Start all services:**
   ```bash
   docker compose up --build
   ```

2. **Wait for startup:**
   - DB2 takes 3-5 minutes to initialize on first run
   - Backend will restart automatically until DB2 is ready
   - Frontend will be available once backend is ready

3. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Health check: http://localhost:8080/health
   - **API Documentation**: http://localhost:8080/swagger-ui.html

4. **First-time setup:**
   - Register a new account
   - Login with your credentials
   - Start uploading and managing photos!

### Development with Hot Reloading

```bash
docker compose -f docker-compose.dev.yml up --build
```

- Frontend (dev): http://localhost:5173
- Backend API: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html

## 🌐 Production Deployment

### AWS Deployment with Terraform

The application includes a complete CI/CD pipeline for AWS deployment:

#### Prerequisites
- AWS CLI configured
- Terraform installed
- Cloudflare account with domain
- SSH key pair

#### Quick Deployment

1. **Setup AWS infrastructure:**
   ```bash
   # Linux/macOS
   ./scripts/setup-aws.sh
   
   # Windows PowerShell
   .\scripts\setup-aws.ps1
   ```

2. **Deploy infrastructure:**
   ```bash
   # Linux/macOS
   ./scripts/terraform-deploy.sh apply
   
   # Windows PowerShell
   .\scripts\terraform-deploy.ps1 -Action apply
   ```

3. **Deploy application:**
   ```bash
   # Linux/macOS
   ./scripts/deploy-production.sh -ServerIP <ec2-ip> -DomainName yourdomain.com
   
   # Windows PowerShell
   .\scripts\deploy-production.ps1 -ServerIP <ec2-ip> -DomainName yourdomain.com
   ```

#### Infrastructure Components
- **VPC and Networking**: Custom VPC with public subnet
- **Compute**: EC2 instance (Ubuntu 22.04 LTS) with Elastic IP
- **Security**: Security groups with appropriate firewall rules
- **DNS**: Cloudflare DNS with automatic SSL certificates
- **Monitoring**: Health checks and logging

📖 **[Complete Deployment Guide](DEPLOYMENT.md)**

## 🔌 API Endpoints

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info
- `PUT /api/auth/profile` - Update user profile

### Photos
- `GET /api/photos` - List photos with pagination
- `GET /api/photos/{id}` - Get photo metadata
- `GET /api/photos/{id}/file` - Download photo file
- `POST /api/photos` - Upload new photo
- `PUT /api/photos/{id}` - Update photo
- `DELETE /api/photos/{id}` - Delete photo
- `DELETE /api/photos/bulk` - Bulk delete photos

### Galleries
- `GET /api/galleries` - List all galleries
- `POST /api/galleries` - Create new gallery
- `GET /api/galleries/{id}` - Get gallery details
- `PUT /api/galleries/{id}` - Update gallery
- `DELETE /api/galleries/{id}` - Delete gallery
- `POST /api/galleries/move-photos` - Move photos between galleries

### Health
- `GET /health` - Application health status

## 🛠️ Local Development

### Backend Only

**With H2 database:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
```

**With DB2 database:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=db2
```

### Frontend Only

```bash
cd frontend
npm install
npm run dev
```

### Full Stack Local Development

1. Start the backend (see above)
2. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
3. Access frontend at http://localhost:5173

## 🐳 Docker Services

### Production Services
- **db2**: IBM DB2 Community Edition database
- **backend**: Spring Boot REST API with JWT authentication
- **frontend**: Vue.js application served by Nginx

### Development Services
- **db2**: Same as production
- **backend**: Same as production
- **frontend-dev**: Vue.js with hot reloading

## 🔧 Scripts and Automation

### Available Scripts

| Script | Purpose | Usage |
|--------|---------|-------|
| `setup-aws.sh/ps1` | Setup AWS infrastructure and prerequisites | `./scripts/setup-aws.sh` |
| `deploy-local.sh/ps1` | Deploy application locally for testing | `./scripts/deploy-local.sh` |
| `deploy-production.sh/ps1` | Deploy to production server | `./scripts/deploy-production.sh -ServerIP <ip>` |
| `terraform-deploy.sh/ps1` | Manage Terraform infrastructure | `./scripts/terraform-deploy.sh apply` |
| `backup-database.sh/ps1` | Backup database with compression | `./scripts/backup-database.sh` |
| `monitor.sh/ps1` | Monitor system health and logs | `./scripts/monitor.sh` |
| `troubleshoot-ami.sh/ps1` | Troubleshoot AWS AMI lookup issues | `./scripts/troubleshoot-ami.sh` |

### PowerShell Support

Windows users have full PowerShell equivalents for all scripts:
- Cross-platform compatibility
- Same functionality as bash scripts
- Windows-specific optimizations

📖 **[PowerShell Scripts Documentation](scripts/README-PowerShell.md)**

## 🔄 CI/CD Pipeline

The application includes a complete GitHub Actions CI/CD pipeline that automatically tests, builds, and deploys the application to AWS.

### 🚀 Pipeline Overview

The CI/CD pipeline consists of two main jobs that run on every push to the `main` branch:

1. **Test Job** - Validates code quality and functionality
2. **Deploy Job** - Provisions infrastructure and deploys the application

### 🧪 Test Job (`test`)

**Triggers**: Every push to `main` branch and pull requests

**Services**:
- **DB2 Database**: IBM DB2 Community Edition container for backend testing
  - Image: `icr.io/db2_community/db2:11.5.8.0`
  - Port: `50000`
  - Environment: Pre-configured with test database `PHOTODB`
  - Health checks: TCP port connectivity with 120s startup period

**Steps**:
1. **Checkout Code**: Downloads the repository code
2. **Setup Java 21**: Installs Temurin JDK 21 for Spring Boot
3. **Cache Maven Dependencies**: Speeds up builds by caching `.m2` directory
4. **Wait for DB2**: Ensures database is ready before testing
5. **Run Backend Tests**: Executes Maven tests with DB2 profile
6. **Setup Node.js**: Installs Node.js 20 for frontend testing
7. **Cache Node Modules**: Caches `node_modules` for faster builds
8. **Install Frontend Dependencies**: Runs `npm ci` for production installs
9. **Run Frontend Tests**: Executes Vue.js unit tests

**Test Configuration**:
```yaml
# Backend tests with DB2
mvn clean test -Dspring.profiles.active=db2 \
  -Ddb.url=jdbc:db2://localhost:50000/PHOTODB \
  -Ddb.user=db2inst1 -Ddb.pass=passw0rd

# Frontend tests
npm run test:unit
```

### 🚀 Deploy Job (`deploy`)

**Triggers**: Only on successful test completion and `main` branch pushes

**Prerequisites**: 
- AWS credentials configured as GitHub Secrets
- Cloudflare API token and zone ID
- SSH private key for EC2 access

**Steps**:

#### 1. **Infrastructure Setup**
- **Checkout Code**: Downloads repository
- **Configure AWS Credentials**: Sets up AWS CLI access
- **Setup Terraform**: Downloads and configures Terraform
- **Setup SSH Key for Terraform**: Creates SSH key from GitHub Secret
- **Get SSH Public Key**: Extracts public key for AWS key pair
- **Handle Existing Key Pair**: Removes old AWS key pairs to prevent conflicts

#### 2. **Infrastructure Provisioning**
- **Terraform Init**: Initializes Terraform backend
- **Terraform Plan**: Shows planned infrastructure changes
- **Terraform Apply**: Creates/updates AWS infrastructure:
  - EC2 instance (t3.micro)
  - Security groups (HTTP/HTTPS/SSH access)
  - Elastic IP address
  - Cloudflare DNS record
  - Uses default VPC to avoid quota limits

#### 3. **Application Deployment**
- **Get EC2 IP**: Retrieves the public IP of the created instance
- **Wait for Instance**: Ensures EC2 instance is running
- **Setup SSH Key for Deployment**: Configures SSH access
- **Deploy Application**: 
  - Copies application files to EC2
  - Installs Docker and Docker Compose if missing
  - Handles package manager conflicts
  - Creates/updates environment configuration
  - Builds and starts Docker containers

#### 4. **Verification**
- **Health Check**: Verifies application is responding
- **Notify Status**: Reports deployment success/failure

### 🔧 Deployment Process Details

#### **File Transfer**
```bash
# Copy files to temporary directory (avoids permission issues)
rsync -avz --delete \
  --exclude='.git' --exclude='node_modules' --exclude='target' \
  ./ ubuntu@$EC2_IP:/tmp/vuedb2springboot/

# Move to final location with sudo
sudo mkdir -p /opt/vuedb2springboot
sudo mv /tmp/vuedb2springboot/* /opt/vuedb2springboot/
sudo chown -R ubuntu:ubuntu /opt/vuedb2springboot
```

#### **Docker Installation & Management**
```bash
# Handle package conflicts
while sudo fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do
  sleep 5
done

# Install Docker if missing
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo systemctl start docker

# Install Docker Compose (both v1 and v2)
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```

#### **Application Startup**
```bash
# Create/update environment file
echo "domain_name=redligot.dev" > .env

# Stop existing containers
sudo docker compose -f docker-compose.prod.yml down || true

# Build and start new containers
sudo docker compose -f docker-compose.prod.yml up -d --build

# Wait for services to be ready
sleep 30
```

### 🔐 Security Features

- **Secrets Management**: All sensitive data stored in GitHub Secrets
- **SSH Key Rotation**: Handles existing key pairs to prevent conflicts
- **Environment Isolation**: Production environment variables managed securely
- **Network Security**: Security groups restrict access to necessary ports only
- **SSL/TLS**: Cloudflare proxy provides automatic SSL termination

### 📊 Monitoring & Health Checks

- **Service Health**: Docker container status monitoring
- **Application Health**: HTTP endpoint health checks
- **Infrastructure Health**: EC2 instance status verification
- **DNS Health**: Cloudflare DNS record validation

### 🚨 Error Handling

- **Package Conflicts**: Waits for and clears package manager locks
- **Docker Issues**: Multiple fallback strategies for Docker installation
- **Network Issues**: Retry logic for health checks
- **Infrastructure Conflicts**: Handles existing AWS resources gracefully

### 📈 Performance Optimizations

- **Caching**: Maven and npm dependency caching
- **Parallel Jobs**: Test and deploy jobs run independently
- **Resource Management**: Efficient Docker container management
- **Build Optimization**: Only rebuilds changed components

### 🔄 Workflow Triggers

```yaml
# Runs on every push to main branch
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
```

### 📋 Required GitHub Secrets

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `AWS_ACCESS_KEY_ID` | AWS access key | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | `wJalr...` |
| `CLOUDFLARE_API_TOKEN` | Cloudflare API token | `abc123...` |
| `CLOUDFLARE_ZONE_ID` | Cloudflare zone ID | `2e75f65a...` |
| `DOMAIN_NAME` | Domain name | `redligot.dev` |
| `SSH_PRIVATE_KEY` | SSH private key | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### 🎯 Success Criteria

The pipeline is considered successful when:
- ✅ All tests pass (backend and frontend)
- ✅ Infrastructure is provisioned without errors
- ✅ Application is deployed and accessible
- ✅ Health checks return successful responses
- ✅ DNS is properly configured

### 🔧 Troubleshooting

**Common Issues**:
- **VPC Limit Exceeded**: Pipeline now uses default VPC to avoid quota limits
- **Docker Installation Failures**: Multiple fallback strategies implemented
- **Package Manager Conflicts**: Automatic lock resolution
- **SSH Key Conflicts**: Automatic cleanup of existing key pairs

**Debug Information**:
- All steps include detailed logging
- Version information for all tools
- Health check results with retry logic
- Infrastructure state validation

## 🔧 Troubleshooting

### Common Issues

**DB2 container fails to start:**
- Ensure Docker Desktop has sufficient resources (4GB+ RAM, 2+ CPUs)
- Increase shared memory: `shm_size: 2g` in docker-compose.yml
- Check logs: `docker compose logs db2`

**Backend can't connect to DB2:**
- DB2 needs 3-5 minutes to initialize on first run
- Backend has restart policy and will retry automatically
- Check DB2 logs: `docker compose logs db2`

**Authentication issues:**
- Clear browser storage and cookies
- Check JWT token expiration
- Verify backend JWT configuration

**Frontend can't connect to backend:**
- Ensure backend is running and healthy
- Check CORS configuration
- Verify API URL in frontend environment variables

**AWS Deployment Issues:**
- Check AWS credentials and permissions
- Verify Terraform state and configuration
- Ensure security group rules allow SSH and HTTP traffic
- Check Cloudflare DNS configuration

**Clean restart:**
```bash
docker compose down -v
docker compose up --build
```

## ⚙️ Environment Variables

### Backend
Override in docker-compose.yml:
- `DB_URL` - JDBC URL (default: jdbc:db2://db2:50000/PHOTODB)
- `DB_USER` - Username (default: db2inst1)
- `DB_PASS` - Password (default: passw0rd)
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRATION` - Token expiration time

### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)

### Production
- `domain_name` - Domain name for SSL and API configuration

## 📁 Project Structure

```
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/redligot/backend/
│   │       ├── controller/  # REST controllers
│   │       ├── service/     # Business logic
│   │       ├── repository/  # Data access
│   │       ├── model/       # JPA entities
│   │       ├── dto/         # Data transfer objects
│   │       ├── security/    # JWT and security config
│   │       └── config/      # Application config
│   └── Dockerfile
├── frontend/                # Vue.js application
│   ├── src/
│   │   ├── components/      # Vue components
│   │   ├── views/           # Page components
│   │   ├── stores/          # Pinia stores (auth, photos, modals)
│   │   ├── services/        # API services
│   │   ├── router/          # Vue Router with guards
│   │   └── utils/           # Utility functions
│   └── Dockerfile
├── scripts/                 # Deployment and utility scripts
│   ├── *.sh                 # Bash scripts
│   ├── *.ps1                # PowerShell scripts
│   └── README-PowerShell.md # PowerShell documentation
├── terraform/               # Infrastructure as Code
│   ├── main.tf              # Main infrastructure
│   ├── variables.tf         # Variable definitions
│   └── terraform.tfvars.example
├── .github/workflows/       # CI/CD pipeline
├── docker-compose.yml       # Production deployment
├── docker-compose.dev.yml   # Development deployment
├── DEPLOYMENT.md            # Deployment guide

├── GALLERY_FEATURE.md       # Gallery features documentation
└── README.md                # This file
```

## 🎮 Usage Guide

### Getting Started
1. **Register/Login** - Create an account or login with existing credentials
2. **Upload Photos** - Use the upload page to add photos to your gallery
3. **Browse Gallery** - View all your photos in the responsive gallery
4. **View Photos** - Click "View" to open the full-screen photo viewer
5. **Manage Photos** - Edit titles, descriptions, or delete photos as needed

### Photo Viewer Controls
- **Mouse wheel** - Zoom in/out
- **Click and drag** - Pan around when zoomed in
- **Arrow keys** - Navigate between photos
- **+/- keys** - Zoom in/out
- **0 key** - Reset zoom
- **Escape** - Return to gallery

### Bulk Operations
- **Select photos** - Use checkboxes to select multiple photos
- **Bulk delete** - Delete selected photos with confirmation
- **Bulk upload** - Upload multiple photos at once
- **Move photos** - Move selected photos between galleries

## 🛡️ Security Features

### Application Security
- **JWT Authentication** - Secure token-based authentication
- **Password Encryption** - BCrypt password hashing
- **CORS Configuration** - Proper cross-origin resource sharing
- **Input Validation** - Server-side validation for all inputs
- **File Upload Security** - Type and size validation

### Infrastructure Security
- **Security Groups** - Restrictive firewall rules
- **SSH Access** - Key-based authentication only
- **SSL/TLS** - Automatic certificate management
- **Secrets Management** - GitHub Secrets for sensitive data
- **Network Isolation** - Private container networking

## 📊 Monitoring and Observability

### Health Checks
- **Backend Health**: `/health` endpoint
- **Frontend Health**: HTTP response check
- **Database Health**: DB2 connection test
- **SSL Certificate**: HTTPS accessibility

### Logging
- **Application Logs**: Spring Boot and Vue.js logs
- **Container Logs**: Docker container logs
- **System Logs**: EC2 instance logs
- **Pipeline Logs**: GitHub Actions execution logs

### Monitoring Scripts
```bash
# Monitor application health
./scripts/monitor.sh

# Continuous monitoring
./scripts/monitor.sh --continuous

# PowerShell equivalent
.\scripts\monitor.ps1 -Continuous
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly (locally and with CI/CD)
5. Submit a pull request

### Development Guidelines
- Follow existing code style and patterns
- Add tests for new features
- Update documentation as needed
- Ensure CI/CD pipeline passes

## 📄 License

This project is licensed under the MIT License.

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vue.js Documentation](https://vuejs.org/)
- [Docker Documentation](https://docs.docker.com/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [Cloudflare Documentation](https://developers.cloudflare.com/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)