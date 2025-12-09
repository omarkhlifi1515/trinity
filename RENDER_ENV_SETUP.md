# 🔧 Render Environment Variables Setup

This document contains the exact environment variables you need to set in your Render dashboard for the Trinity HRM application.

## 📋 Required Environment Variables

Copy and paste these into your Render service's Environment tab:

### Application Configuration

```env
APP_NAME=Trinity HRM
APP_ENV=production
APP_KEY=
APP_DEBUG=false
APP_TIMEZONE=UTC
APP_URL=https://your-service-name.onrender.com
APP_LOCALE=en
APP_FALLBACK_LOCALE=en
```

**Important**: Generate `APP_KEY` by running this command locally or in Render shell:
```bash
php artisan key:generate --show
```
Copy the output (starts with `base64:`) and set it as `APP_KEY`.

### Database Configuration (PostgreSQL)

Use the **Internal Database URL** for better performance (same region):

```env
DB_CONNECTION=pgsql
DB_HOST=dpg-d4s6qmmmcj7s73fhs4vg-a
DB_PORT=5432
DB_DATABASE=rinity_hrm
DB_USERNAME=rinity_hrm_user
DB_PASSWORD=yv46EXgzxbAK3vBRod9SifxmHtS08pQX
```

**Alternative**: You can also use the full connection string:
```env
DATABASE_URL=postgresql://rinity_hrm_user:yv46EXgzxbAK3vBRod9SifxmHtS08pQX@dpg-d4s6qmmmcj7s73fhs4vg-a/rinity_hrm
```

### Session & Cache Configuration

```env
SESSION_DRIVER=database
SESSION_LIFETIME=120
CACHE_STORE=database
QUEUE_CONNECTION=database
```

### Logging Configuration

```env
LOG_CHANNEL=stack
LOG_STACK=single
LOG_LEVEL=error
```

### Mail Configuration (Optional - Update with your SMTP settings)

```env
MAIL_MAILER=smtp
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=null
MAIL_PASSWORD=null
MAIL_ENCRYPTION=null
MAIL_FROM_ADDRESS=noreply@trinityhrm.com
MAIL_FROM_NAME="Trinity HRM"
```

### OpenAI Configuration (Optional - for ChatGPT Bot)

```env
OPENAI_API_KEY=your_openai_key_here
FILAMENT_CHATGPT_BOT_ENABLED=true
FILAMENT_CHATGPT_BOT_API_KEY=your_openai_key_here
FILAMENT_CHATGPT_BOT_MODEL=gpt-4
```

## 🚀 Quick Setup Steps

1. **Go to your Render service dashboard**
2. **Click on "Environment" tab**
3. **Add each variable** from the sections above
4. **Save changes** - Render will automatically redeploy

## 📝 Notes

- **Internal vs External Database URL**: 
  - Use **Internal URL** (`dpg-d4s6qmmmcj7s73fhs4vg-a`) when your web service is in the same region
  - Use **External URL** (`dpg-d4s6qmmmcj7s73fhs4vg-a.frankfurt-postgres.render.com`) for external access or different regions
  
- **APP_URL**: Update this with your actual Render service URL after deployment

- **Database Connection**: The app is configured to use PostgreSQL. Make sure your `DB_CONNECTION=pgsql` is set.

## 🔍 Verify Database Connection

After setting environment variables, you can test the connection in Render shell:

```bash
php artisan tinker
>>> DB::connection()->getPdo();
```

If successful, you'll see the PDO object. If it fails, check your database credentials.

## 🗄️ Run Migrations

After your first deployment, run migrations in Render shell:

```bash
php artisan migrate --force
php artisan db:seed
```

---

**Security Note**: Never commit these credentials to Git. Always use Render's environment variables for sensitive data.

