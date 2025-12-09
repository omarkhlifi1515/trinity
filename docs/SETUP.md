# 🚀 Setup Guide - Trinity Get 2

Complete setup guide for both web and mobile applications.

## 📋 Prerequisites

- Node.js 18+ installed
- npm or yarn package manager
- Supabase account (already configured)
- For mobile: Expo CLI (`npm install -g expo-cli`)

## 🌐 Web App Setup

### 1. Navigate to web directory
```bash
cd web
```

### 2. Install dependencies
```bash
npm install
```

### 3. Environment variables
Create `.env.local` file (already created with your Supabase credentials):
```env
NEXT_PUBLIC_SUPABASE_URL=https://nghwpwajcoofbgvsevgf.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx
```

### 4. Run development server
```bash
npm run dev
```

The web app will be available at `http://localhost:3000`

## 📱 Mobile App Setup

### 1. Navigate to mobile directory
```bash
cd mobile
```

### 2. Install dependencies
```bash
npm install
```

### 3. Install additional dependencies
```bash
npm install @react-native-async-storage/async-storage
npm install lucide-react-native
```

### 4. Run on device/emulator

**iOS:**
```bash
npm run ios
```

**Android:**
```bash
npm run android
```

**Web (for testing):**
```bash
npm run web
```

## 🗄️ Supabase Setup

### Database Schema

You'll need to create these tables in your Supabase project:

1. **users** - Extended user profiles
2. **employees** - Employee information
3. **tasks** - Task management
4. **attendance** - Attendance records
5. **leaves** - Leave requests

### Enable Authentication

1. Go to Supabase Dashboard → Authentication
2. Enable Email/Password authentication
3. Configure email templates (optional)

## 🎨 Design System

Both apps share the same design system:

- **Primary Color**: Blue (#3b82f6)
- **Secondary Color**: Purple (#8b5cf6)
- **Success Color**: Green (#10b981)
- **Error Color**: Red (#ef4444)
- **Background**: Light Gray (#f9fafb)

## 🔐 Authentication Flow

1. User signs up/signs in via Supabase Auth
2. Session is stored locally
3. Both apps share the same authentication state
4. Real-time sync across devices

## 📚 Next Steps

1. Set up Supabase database schema
2. Implement CRUD operations
3. Add real-time subscriptions
4. Deploy web app (Vercel/Netlify)
5. Build mobile app for production

---

For detailed documentation, see:
- [Web App README](../web/README.md)
- [Mobile App README](../mobile/README.md)

