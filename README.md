# Photo Gallery Application

A full-stack photo gallery application with Spring Boot backend and Vue.js frontend, featuring JWT authentication, photo management, and advanced viewing capabilities.

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

## 🚀 Quick Start with Docker

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

4. **First-time setup:**
   - Register a new account
   - Login with your credentials
   - Start uploading and managing photos!

### Development Deployment

For development with hot reloading:

```bash
docker compose -f docker-compose.dev.yml up --build
```

- Frontend (dev): http://localhost:5173
- Backend API: http://localhost:8080

## 🔌 API Endpoints

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

### Health
- `GET /health` - Application health status

## 🎯 Frontend Features

### Core Functionality
- 📸 **Photo Gallery** - Responsive grid layout with lazy loading
- 🔐 **Authentication** - Login/register with JWT token management
- 👤 **Profile Management** - Update user information
- 📤 **Photo Upload** - Single and bulk upload with drag-and-drop
- ✏️ **Photo Editing** - Update titles and descriptions
- 🗑️ **Photo Deletion** - Single and bulk deletion with confirmation
- 🔍 **Photo Viewing** - Full-screen viewer with zoom and pan

### Advanced Features
- 🔄 **Real-time updates** - Automatic gallery refresh after operations
- 📱 **Responsive design** - Optimized for all screen sizes
- ⚡ **Performance** - Lazy loading, optimized image delivery
- 🎨 **Modern UI** - Smooth animations, hover effects, dark theme
- ⌨️ **Keyboard shortcuts** - Navigation and zoom controls
- 📊 **Pagination** - Configurable page sizes with sticky navigation

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

## 🔧 Troubleshooting

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
- `APP_JWT_SECRET` - JWT signing secret (default: your-secret-key)

### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)

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
│   │   └── types/           # TypeScript types
│   └── Dockerfile
├── docker-compose.yml       # Production deployment
├── docker-compose.dev.yml   # Development deployment
└── README.md
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

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.
