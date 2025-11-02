#!/bin/bash

# Tartan Smart Home Rollback Script
# This script reverts the backend services to the previous version
# Usage: ./rollback.sh

set -e

echo "=========================================="
echo "Tartan Smart Home - Rollback Script"
echo "=========================================="
echo ""

# Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker is not installed or not in PATH"
    exit 1
fi

# Check if docker compose is available
if ! docker compose version &> /dev/null; then
    echo "❌ Error: Docker Compose is not installed or not available"
    exit 1
fi

echo "📋 Step 1: Checking for previous version images..."

# Check if previous version images exist
PLATFORM_PREV=$(docker images -q smart-home-platform:prev 2>/dev/null || echo "")
MYSQL_PREV=$(docker images -q smart-home-mysql-container:prev 2>/dev/null || echo "")

if [ -z "$PLATFORM_PREV" ]; then
    echo "⚠️  Warning: No previous version found for smart-home-platform"
fi

if [ -z "$MYSQL_PREV" ]; then
    echo "⚠️  Warning: No previous version found for smart-home-mysql-container"
fi

if [ -z "$PLATFORM_PREV" ] && [ -z "$MYSQL_PREV" ]; then
    echo "❌ Error: No previous versions found. Cannot rollback."
    echo "   This might be the first deployment, or previous images were cleaned up."
    exit 1
fi

echo "✅ Previous versions found!"
echo ""

echo "📦 Current running images:"
docker compose -f docker-compose-backend.yml images 2>/dev/null || docker compose images 2>/dev/null || echo "No containers running"
echo ""

echo "🔄 Step 2: Tagging previous versions as active..."

# Tag previous versions as active
if [ -n "$PLATFORM_PREV" ]; then
    docker tag smart-home-platform:prev smart-home-platform:active
    echo "✅ Tagged smart-home-platform:prev -> smart-home-platform:active"
fi

if [ -n "$MYSQL_PREV" ]; then
    docker tag smart-home-mysql-container:prev smart-home-mysql-container:active
    echo "✅ Tagged smart-home-mysql-container:prev -> smart-home-mysql-container:active"
fi

echo ""
echo "🚀 Step 3: Restarting backend services with previous versions..."

# Restart the backend services
docker compose -f docker-compose-backend.yml down 2>/dev/null || true
docker compose -f docker-compose-backend.yml up -d

echo ""
echo "=========================================="
echo "✅ Rollback completed successfully!"
echo "=========================================="
echo ""
echo "Backend services have been reverted to the previous version."
echo ""
echo "📊 Verify the rollback with:"
echo "   docker compose -f docker-compose-backend.yml images"
echo ""
echo "🔍 Check service health:"
echo "   docker compose -f docker-compose-backend.yml ps"
echo ""
echo "📝 View logs:"
echo "   docker compose -f docker-compose-backend.yml logs -f"
echo ""

