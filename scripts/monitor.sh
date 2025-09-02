#!/bin/bash

# Monitoring Script for VueDB2SpringBoot
set -e

echo "VueDB2SpringBoot System Monitor"
echo "=================================="

# Check Docker containers
echo "Docker Containers Status:"
docker-compose ps

echo ""

# Check system resources
echo "System Resources:"
echo "CPU Usage:"
top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1

echo "Memory Usage:"
free -h

echo "Disk Usage:"
df -h /

echo ""

# Check application health
echo "Application Health Checks:"

# Backend health
if curl -f -s http://localhost:8080/api/health > /dev/null; then
    echo "Backend: Healthy"
else
    echo "Backend: Unhealthy"
fi

# Frontend health
if curl -f -s http://localhost:3000 > /dev/null; then
    echo "Frontend: Healthy"
else
    echo "Frontend: Unhealthy"
fi

# Database health
if docker exec db2 db2 connect to PHOTODB user db2inst1 using passw0rd > /dev/null 2>&1; then
    echo "Database: Healthy"
    docker exec db2 db2 terminate > /dev/null 2>&1
else
    echo "Database: Unhealthy"
fi

echo ""

# Check logs for errors
echo "Recent Error Logs:"
echo "Backend errors:"
docker-compose logs --tail=10 backend | grep -i error || echo "No recent backend errors"

echo "Frontend errors:"
docker-compose logs --tail=10 frontend | grep -i error || echo "No recent frontend errors"

echo "Database errors:"
docker-compose logs --tail=10 db2 | grep -i error || echo "No recent database errors"

echo ""
echo "Monitoring complete!"
