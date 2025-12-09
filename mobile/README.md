# Trinity HRM Mobile App (React Native)

React Native mobile application built with Expo Router and local authentication.

## Features

- ✅ Local authentication (no Supabase needed!)
- ✅ Same API as web app
- ✅ Offline support with AsyncStorage
- ✅ Consistent theme with web app
- ✅ Navigation with Expo Router

## Tech Stack

- **Framework**: React Native with Expo
- **Navigation**: Expo Router
- **UI**: React Native Paper
- **State Management**: Zustand
- **Authentication**: Local API (connects to web app)

## Setup

### Prerequisites

- Node.js 18+
- Expo CLI: `npm install -g expo-cli`
- iOS Simulator (Mac) or Android Studio (for Android)

### Installation

```bash
cd mobile
npm install
```

### Configuration

Create a `.env` file in the `mobile` folder:

```env
EXPO_PUBLIC_API_URL=http://localhost:3000
```

**For physical devices:**
- Replace `localhost` with your computer's IP address
- Example: `EXPO_PUBLIC_API_URL=http://192.168.1.100:3000`

### Running the App

```bash
npm start
```

Then:
- Press `i` for iOS simulator
- Press `a` for Android emulator
- Scan QR code with Expo Go app (physical device)

## How It Works

The mobile app connects to your web app's API endpoints:

- `/api/auth/login` - Login
- `/api/auth/signup` - Signup
- `/api/auth/logout` - Logout
- `/api/auth/me` - Get current user

User data is stored in AsyncStorage for offline access.

## Project Structure

```
mobile/
├── app/
│   ├── (auth)/          # Auth screens (login, signup)
│   ├── (tabs)/          # Main app screens
│   └── _layout.tsx      # Root layout
├── lib/
│   └── supabase.ts      # API client (renamed, no Supabase!)
├── store/
│   └── authStore.ts     # Auth state management
└── constants/
    └── theme.ts         # App theme
```

## Notes

- The mobile app requires the web app to be running
- Make sure both apps use the same authentication system
- No Supabase configuration needed!
