# Photo Upload Service

A Spring Boot REST API for photo upload and management with DB2 database support.

## Quick Start with Docker

### Prerequisites
- Docker Desktop with at least 4GB RAM and 2 CPUs allocated
- Docker Compose

### Running the Application

1. **Start the services:**
   ```bash
   docker compose up --build
   ```

2. **Wait for startup:**
   - DB2 takes 3-5 minutes to initialize on first run
   - Backend will restart automatically until DB2 is ready
   - Check logs: `docker compose logs -f`

3. **Test the API:**
   ```bash
   # Health check
   curl http://localhost:8080/health
   
   # List photos
   curl http://localhost:8080/api/photos
   ```

### API Endpoints

- `GET /health` - Application health status
- `GET /api/photos` - List all photos
- `GET /api/photos/{id}` - Get photo metadata
- `GET /api/photos/{id}/metadata` - Get photo metadata only
- `GET /api/photos/{id}/file` - Download photo file
- `POST /api/photos` - Upload new photo (multipart: title, description?, file)
- `PUT /api/photos/{id}` - Update photo (multipart: title, description?, file?)
- `DELETE /api/photos/{id}` - Delete photo

### Troubleshooting

**DB2 container fails to start:**
- Ensure Docker Desktop has sufficient resources (4GB+ RAM, 2+ CPUs)
- Increase shared memory: `shm_size: 2g` in docker-compose.yml
- Check logs: `docker compose logs db2`

**Backend can't connect to DB2:**
- DB2 needs 3-5 minutes to initialize on first run
- Backend has restart policy and will retry automatically
- Check DB2 logs: `docker compose logs db2`

**Clean restart:**
```bash
docker compose down -v
docker compose up --build
```

### Development

**Local development with H2:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=default
```

**Local development with DB2:**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=db2
```

### Environment Variables

Override DB2 connection in docker-compose.yml:
- `DB_URL` - JDBC URL (default: jdbc:db2://db2:50000/PHOTODB)
- `DB_USER` - Username (default: db2inst1)
- `DB_PASS` - Password (default: passw0rd)
