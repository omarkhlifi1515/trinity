#!/bin/bash
set -e

echo "🚀 Starting Trinity HRM..."

# Wait for database to be ready (if needed)
# You can add database connection check here

# Cache Laravel configuration
echo "⚙️ Caching configuration..."
php artisan config:cache || true
php artisan route:cache || true
php artisan view:cache || true

# Run migrations (optional - uncomment if you want auto-migrations)
# php artisan migrate --force || true

# Start Laravel server
echo "✅ Starting server on port ${PORT:-8080}..."
exec php artisan serve --host=0.0.0.0 --port=${PORT:-8080}

