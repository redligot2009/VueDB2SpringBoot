# Photo Gallery Frontend

A Vue.js frontend application for displaying photos from the Spring Boot backend.

## Features

- 📸 Photo gallery with responsive grid layout
- 🔄 Real-time photo loading from backend API
- 📱 Responsive design for mobile and desktop
- ⚡ Fast loading with lazy image loading
- 🎨 Modern UI with hover effects and animations

## Prerequisites

- Node.js (version 16 or higher)
- npm or yarn
- Spring Boot backend running on `http://localhost:8080`

## Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

3. Open your browser and navigate to `http://localhost:5173`

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run format` - Format code with Prettier
- `npm run lint` - Lint code with ESLint

## Project Structure

```
src/
├── components/
│   └── PhotoCard.vue          # Individual photo card component
├── views/
│   └── PhotoGallery.vue       # Main gallery view
├── stores/
│   └── photoStore.ts          # Pinia store for photo state management
├── services/
│   └── api.ts                 # API service for backend communication
├── router/
│   └── index.ts               # Vue Router configuration
└── App.vue                    # Root component
```

## API Integration

The frontend communicates with the Spring Boot backend through the following endpoints:

- `GET /api/photos` - Get all photos
- `GET /api/photos/{id}` - Get photo by ID
- `GET /api/photos/{id}/metadata` - Get photo metadata only
- `GET /api/photos/{id}/file` - Get photo image data
- `POST /api/photos` - Create new photo
- `PUT /api/photos/{id}` - Update photo
- `DELETE /api/photos/{id}` - Delete photo

## Technologies Used

- **Vue 3** - Progressive JavaScript framework
- **TypeScript** - Type-safe JavaScript
- **Vite** - Fast build tool and dev server
- **Pinia** - State management
- **Vue Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **ESLint** - Code linting
- **Prettier** - Code formatting

## Development

### Adding New Features

1. Create components in `src/components/`
2. Add views in `src/views/`
3. Update the router in `src/router/index.ts`
4. Add API methods in `src/services/api.ts`
5. Update the store in `src/stores/photoStore.ts`

### Styling

The application uses scoped CSS with modern design principles:
- Responsive grid layouts
- CSS Grid and Flexbox
- Smooth transitions and animations
- Mobile-first approach

## Deployment

1. Build the application:
```bash
npm run build
```

2. The built files will be in the `dist/` directory

3. Deploy the contents of `dist/` to your web server

## Troubleshooting

### CORS Issues
Make sure the Spring Boot backend has CORS configured to allow requests from `http://localhost:5173`.

### API Connection Issues
- Verify the backend is running on `http://localhost:8080`
- Check the API base URL in `src/services/api.ts`
- Ensure all required endpoints are available

### Build Issues
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Check Node.js version compatibility
- Verify all dependencies are properly installed
