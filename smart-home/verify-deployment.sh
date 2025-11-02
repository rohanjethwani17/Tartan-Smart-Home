#!/bin/bash

# Tartan Smart Home Deployment Verification Script
# This script helps verify which version of the system is currently deployed

echo "=========================================="
echo "Tartan Smart Home - Deployment Status"
echo "=========================================="
echo ""

echo "Currently Running Containers:"
docker compose -f docker-compose-backend.yml ps
echo ""

echo "Active Docker Images:"
docker compose -f docker-compose-backend.yml images
echo ""

echo "Available Image Versions:"
echo ""
echo "Platform Images:"
docker images smart-home-platform --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}"
echo ""
echo "MySQL Images:"
docker images smart-home-mysql-container --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}"
echo ""

echo "Image History:"
PLATFORM_ACTIVE=$(docker images -q smart-home-platform:active 2>/dev/null || echo "")
PLATFORM_PREV=$(docker images -q smart-home-platform:prev 2>/dev/null || echo "")

if [ -n "$PLATFORM_ACTIVE" ]; then
    echo "Active platform image: $PLATFORM_ACTIVE"
else
    echo "No active platform image found"
fi

if [ -n "$PLATFORM_PREV" ]; then
    echo "Previous platform image: $PLATFORM_PREV"
else
    echo "No previous platform image found (rollback not available)"
fi

echo ""
echo "=========================================="

