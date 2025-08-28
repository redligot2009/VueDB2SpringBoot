# Photo Upload Service

A full-stack photo upload and gallery application with Spring Boot backend and Vue.js frontend.

## Architecture

- **Backend**: Spring Boot REST API with DB2 database
- **Frontend**: Vue.js 3 with TypeScript and Vite
- **Database**: IBM DB2 Community Edition
- **Containerization**: Docker with Docker Compose

## Quick Start with Docker

### Prerequisites
- Docker Desktop with at least 4GB RAM and 2 CPUs allocated
- Docker Compose

### Production Deployment

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

### Development Deployment

For development with hot reloading:

```bash
docker compose -f docker-compose.dev.yml up --build
```

- Frontend (dev): http://localhost:5173
- Backend API: http://localhost:8080

## API Endpoints

- `GET /health` - Application health status
- `GET /api/photos` - List all photos
- `GET /api/photos/{id}` - Get photo metadata
- `GET /api/photos/{id}/metadata` - Get photo metadata only
- `GET /api/photos/{id}/file` - Download photo file
- `POST /api/photos` - Upload new photo (multipart: title, description?, file)
- `PUT /api/photos/{id}` - Update photo (multipart: title, description?, file?)
- `DELETE /api/photos/{id}` - Delete photo

## Frontend Features

- 📸 Photo gallery with responsive grid layout
- 🔄 Real-time photo loading from backend API
- 📱 Responsive design for mobile and desktop
- ⚡ Fast loading with lazy image loading
- 🎨 Modern UI with hover effects and animations

## Local Development

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

## Docker Services

### Production Services
- **db2**: IBM DB2 Community Edition database
- **backend**: Spring Boot REST API
- **frontend**: Vue.js application served by Nginx

### Development Services
- **db2**: Same as production
- **backend**: Same as production
- **frontend-dev**: Vue.js with hot reloading

## Troubleshooting

**DB2 container fails to start:**
- Ensure Docker Desktop has sufficient resources (4GB+ RAM, 2+ CPUs)
- Increase shared memory: `shm_size: 2g` in docker-compose.yml
- Check logs: `docker compose logs db2`

**Backend can't connect to DB2:**
- DB2 needs 3-5 minutes to initialize on first run
- Backend has restart policy and will retry automatically
- Check DB2 logs: `docker compose logs db2`

**Frontend can't connect to backend:**
- Ensure backend is running and healthy
- Check CORS configuration
- Verify API URL in frontend environment variables

**Clean restart:**
```bash
docker compose down -v
docker compose up --build
```

## Environment Variables

### Backend
Override DB2 connection in docker-compose.yml:
- `DB_URL` - JDBC URL (default: jdbc:db2://db2:50000/PHOTODB)
- `DB_USER` - Username (default: db2inst1)
- `DB_PASS` - Password (default: passw0rd)

### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)

## Project Structure

```
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   │   └── com/redligot/backend/
│   │       ├── controller/  # REST controllers
│   │       ├── service/     # Business logic
│   │       ├── repository/  # Data access
│   │       ├── model/       # JPA entities
│   │       └── config/      # Configuration
│   └── Dockerfile
├── frontend/                # Vue.js application
│   ├── src/
│   │   ├── components/      # Vue components
│   │   ├── views/           # Page components
│   │   ├── stores/          # Pinia stores
│   │   ├── services/        # API services
│   │   └── router/          # Vue Router
│   └── Dockerfile
├── docker-compose.yml       # Production deployment
├── docker-compose.dev.yml   # Development deployment
└── README.md
```
