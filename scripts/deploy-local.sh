#!/bin/bash

# Local Deployment Script for VueDB2SpringBoot
set -e

echo "Starting local deployment..."

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "ERROR: Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "ERROR: Docker Compose is not installed. Please install it first."
    exit 1
fi

echo "Docker and Docker Compose are available"

# Build and start services
echo "Building and starting services..."
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 30

# Check service health
echo "Checking service health..."
docker-compose ps

# Test backend health
echo "Testing backend health..."
if curl -f -s http://localhost:8080/health; then
    echo "Backend is healthy"
else
    echo "ERROR: Backend health check failed"
    docker-compose logs backend
    exit 1
fi

# Test frontend
echo "Testing frontend..."
if curl -f -s http://localhost:3000; then
    echo "Frontend is accessible"
else
    echo "ERROR: Frontend is not accessible"
    docker-compose logs frontend
    exit 1
fi

echo "Local deployment successful!"
echo "Frontend: http://localhost:3000"
echo "Backend API: http://localhost:8080/api"
echo "Health Check: http://localhost:8080/health"
echo "API Docs: http://localhost:8080/swagger-ui.html"
