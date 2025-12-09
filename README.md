# 🚀 Trinity Get 2 - Unified HRM Platform

A modern Human Resource Management System with **web**, **React Native mobile**, and **Kotlin Android mobile** applications sharing the same Supabase database.

## 📱 Applications

### 1. Web App (Next.js)
- Modern Next.js 14+ application
- Tailwind CSS styling
- Responsive design
- Location: `web/`

### 2. React Native Mobile App
- Cross-platform mobile app
- Expo framework
- Material Design UI
- Location: `mobile/`

### 3. Kotlin Android Mobile App
- Native Android application
- Jetpack Compose UI
- Material Design 3
- Location: `mobile-kotlin/`

## 🎨 Shared Features

All three applications share:
- ✅ **Same Supabase Database** - Unified data across all platforms
- ✅ **Same Authentication** - Sign in once, access everywhere
- ✅ **Same Design Theme** - Consistent UI/UX
- ✅ **Real-time Sync** - Changes reflect instantly across platforms
- ✅ **Direct Database Access** - Mobile apps connect directly (no API server needed)

## 🛠 Tech Stack

### Web
- Next.js 14+ (App Router)
- TypeScript
- Tailwind CSS
- Supabase JS Client

### React Native Mobile
- React Native
- Expo
- TypeScript
- React Native Paper
- Supabase JS Client

### Kotlin Android Mobile
- Kotlin
- Jetpack Compose
- Material Design 3
- Supabase Kotlin Client
- MVVM Architecture

## 📋 Supabase Configuration

All apps use the same Supabase project:
- **URL**: `https://nghwpwajcoofbgvsevgf.supabase.co`
- **Anon Key**: `sb_publishable_hahT_e8_6T-6qXE4boTyYQ_Q-w5rFzx`

## 🎨 Design System

Unified theme across all platforms:
- **Primary**: Blue (#3b82f6)
- **Secondary**: Purple (#8b5cf6)
- **Success**: Green (#10b981)
- **Error**: Red (#ef4444)
- **Background**: Light Gray (#f9fafb)

## 🚀 Quick Start

### Web App
```bash
cd web
npm install
npm run dev
```

### React Native Mobile
```bash
cd mobile
npm install
npm start
```

### Kotlin Android Mobile
1. Open `mobile-kotlin` in Android Studio
2. Sync Gradle files
3. Run on device/emulator

## 📚 Documentation

- [Web App README](./web/README.md)
- [React Native Mobile README](./mobile/README.md)
- [Kotlin Android Mobile README](./mobile-kotlin/README.md)
- [Setup Guide](./docs/SETUP.md)

## 🔐 Authentication Flow

1. User signs up/signs in via any app
2. Session stored in Supabase Auth
3. All apps share the same authentication state
4. Real-time sync across all platforms

## 📱 Key Features

- Employee Management
- Task Management
- Attendance Tracking
- Leave Management
- Real-time Updates
- Offline Support (mobile apps)

---

Made with ❤️ using modern technologies
