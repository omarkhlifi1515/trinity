#!/bin/bash

if [ -d "/home/frappe/frappe-bench/apps/frappe" ]; then
    echo "Bench already exists, skipping init"
    cd frappe-bench
    bench start
else
    echo "Creating new bench..."
fi

export PATH="${NVM_DIR}/versions/node/v${NODE_VERSION_DEVELOP}/bin/:${PATH}"

# Ensure Postgres driver for Python is available for bench
pip install psycopg2-binary

bench init --skip-redis-config-generation frappe-bench

cd frappe-bench

# Configure DB/Redis endpoints (DB values come from environment variables provided to the container)
# Note: bench commands historically reference mariadb naming; here we set host/port using env vars
bench set-mariadb-host ${DB_HOST}
bench set-mariadb-port ${DB_PORT} || true
bench set-redis-cache-host redis://redis:6379
bench set-redis-queue-host redis://redis:6379
bench set-redis-socketio-host redis://redis:6379

# Remove redis, watch from Procfile
sed -i '/redis/d' ./Procfile
sed -i '/watch/d' ./Procfile

bench get-app erpnext
bench get-app hrms

# Install local custom app into the bench virtualenv (editable)
if [ -d "apps/hrms" ]; then
  ./env/bin/pip install -e apps/hrms
fi

# Ensure Supabase/Postgres will be used with SSL by injecting db_ssl into common_site_config.json
python - <<'PY'
import json, os
p = os.path.join('sites', 'common_site_config.json')
data = {}
if os.path.exists(p):
    try:
        with open(p, 'r') as f:
            data = json.load(f) or {}
    except Exception:
        data = {}
else:
    os.makedirs(os.path.dirname(p), exist_ok=True)
data['db_ssl'] = True
with open(p, 'w') as f:
    json.dump(data, f, indent=2)
PY

# Create new site connecting to external Postgres (Supabase)
bench new-site hrms.localhost \
  --force \
  --db-type postgres \
  --no-mariadb-socket \
  --db-host ${DB_HOST} \
  --db-port ${DB_PORT} \
  --db-root-username ${DB_USER} \
  --db-root-password ${DB_PASSWORD} \
  --admin-password admin

bench --site hrms.localhost install-app hrms
bench --site hrms.localhost set-config developer_mode 1
bench --site hrms.localhost enable-scheduler
bench --site hrms.localhost clear-cache
bench use hrms.localhost

bench start