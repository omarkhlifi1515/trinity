# Local Development Setup

## Quick Start

1. **Install dependencies:**
   ```bash
   cd web
   npm install
   ```

2. **Set up environment variables:**
   Create a `.env` file in the `web` directory:
   ```env
   JSONBIN_API_KEY=your_jsonbin_master_key_here
   AUTH_SECRET=your-secret-key-change-this-min-32-chars-long
   ```

3. **Get your JSONBin API Key:**
   - Go to https://jsonbin.io/
   - Sign up for a free account
   - Get your Master Key from the dashboard
   - Add it to `.env` as `JSONBIN_API_KEY`

4. **Run the development server:**
   ```bash
   npm run dev
   ```

5. **Open your browser:**
   - Navigate to http://localhost:3000
   - The app will automatically create a JSONBin bin on first use

## How It Works

### Data Storage
- **Primary**: JSONBin.io (cloud storage, shared with mobile apps)
- **Fallback**: Local file (`web/data/users.json`) for offline development
- **Auto-sync**: Data is automatically synced to JSONBin.io

### Shared Database
All three apps share the same JSONBin.io database:
- ✅ Web app (Next.js) - This app
- ✅ React Native mobile app
- ✅ Kotlin Android app

### First Time Setup
1. When you first sign up a user, the app will:
   - Create a new JSONBin bin automatically
   - Save the bin ID to `web/data/jsonbin-id.txt`
   - Store all users in that bin

2. The bin ID will be shown in the console:
   ```
   ✅ Created new JSONBin: 65abc123def456...
   💡 Add this to your .env: JSONBIN_BIN_ID=65abc123def456...
   ```

3. (Optional) Add the bin ID to `.env` for faster loading:
   ```env
   JSONBIN_BIN_ID=your_bin_id_here
   ```

## Environment Variables

### Required
- `JSONBIN_API_KEY` - Your JSONBin.io Master Key
- `AUTH_SECRET` - Secret for JWT tokens (min 32 characters)

### Optional
- `JSONBIN_BIN_ID` - Your existing bin ID (auto-created if not set)

## Development Commands

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Lint code
npm run lint
```

## Troubleshooting

### "JSONBin API key not set"
- Make sure `.env` exists in the `web` directory
- Check that `JSONBIN_API_KEY` is set correctly
- Restart the dev server after changing `.env`

### "Failed to create bin"
- Verify your API key is correct
- Check your internet connection
- Make sure the API key has write permissions

### Data not syncing
- Check browser console for errors
- Verify JSONBin API key is valid
- Check network tab for API requests

## File Structure

```
web/
├── .env                # Environment variables (not in git)
├── data/               # Local data storage (auto-created)
│   ├── users.json      # Local user backup
│   └── jsonbin-id.txt  # JSONBin bin ID
├── lib/
│   ├── auth/           # Authentication logic
│   └── storage/        # JSONBin.io integration
└── app/                # Next.js app directory
```

## Notes

- Data is stored in JSONBin.io cloud storage
- All three apps (web, React Native, Kotlin) share the same database
- Local files are just backups/fallbacks
- No database setup needed - JSONBin.io handles everything!

