# Mobile App Setup

The mobile app now uses local authentication (no Supabase needed!).

## Configuration

### Step 1: Set API URL

Create a `.env` file in the `mobile` folder:

```env
EXPO_PUBLIC_API_URL=http://localhost:3000
```

**For production**, change it to your deployed web app URL:
```env
EXPO_PUBLIC_API_URL=https://your-domain.com
```

### Step 2: Install Dependencies

```bash
cd mobile
npm install
```

### Step 3: Run the App

```bash
npm start
```

## How It Works

- The mobile app connects to your web app's API endpoints
- Authentication uses the same local auth system
- User data is stored in AsyncStorage for offline access
- Cookies are handled automatically by the API

## Troubleshooting

### "Network request failed"
- Make sure your web app is running (`npm run dev` in the `web` folder)
- Check that `EXPO_PUBLIC_API_URL` matches your web app URL
- For physical devices, use your computer's IP address instead of `localhost`

### "Cannot connect to API"
- If using a physical device, replace `localhost` with your computer's IP:
  - Windows: `ipconfig` → find IPv4 address
  - Mac/Linux: `ifconfig` → find inet address
  - Example: `EXPO_PUBLIC_API_URL=http://192.168.1.100:3000`

### Authentication not working
- Make sure you've created an account on the web app first
- Check that the web app API is accessible from your device
- Clear app data and try again

## Notes

- The mobile app stores auth tokens in AsyncStorage
- User sessions persist across app restarts
- No Supabase configuration needed!

