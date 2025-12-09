#!/bin/bash
set -e

echo "🚀 Building Trinity HRM for Render..."

# Install PHP dependencies
echo "📦 Installing PHP dependencies..."
composer install --no-dev --optimize-autoloader

# Install Node dependencies
echo "📦 Installing Node dependencies..."
npm ci

# Build frontend assets
echo "🔨 Building frontend assets..."
npm run build

# Cache configuration
echo "⚙️ Caching configuration..."
php artisan config:cache
php artisan route:cache
php artisan view:cache

# Generate Filament assets
echo "🎨 Generating Filament assets..."
php artisan filament:assets

# Run migrations (if needed)
echo "🗄️ Running migrations..."
php artisan migrate --force

echo "✅ Build complete!"

