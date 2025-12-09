# Trinity HRM Mobile App (Kotlin/Android)

Native Android application built with Kotlin, Jetpack Compose, and local authentication.

## Features

- ✅ Local authentication (no Supabase needed!)
- ✅ Same API as web app
- ✅ Modern UI with Jetpack Compose
- ✅ MVVM architecture
- ✅ Consistent theme with web app

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **HTTP Client**: Ktor
- **Navigation**: Navigation Compose
- **Authentication**: Local API (connects to web app)

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 26+

### Configuration

Update the API URL in `ApiClient.kt`:

```kotlin
// For Android emulator:
private const val BASE_URL = "http://10.0.2.2:3000"

// For physical device (use your computer's IP):
private const val BASE_URL = "http://192.168.1.100:3000"

// For production:
private const val BASE_URL = "https://your-domain.com"
```

### Building

1. Open project in Android Studio
2. Sync Gradle files
3. Run on emulator or device

## How It Works

The mobile app connects to your web app's API endpoints:

- `/api/auth/login` - Login
- `/api/auth/signup` - Signup
- `/api/auth/logout` - Logout
- `/api/auth/me` - Get current user

## Project Structure

```
mobile-kotlin/
├── app/src/main/java/com/trinity/hrm/
│   ├── data/
│   │   └── remote/
│   │       └── ApiClient.kt      # API client (no Supabase!)
│   ├── ui/
│   │   ├── auth/                 # Auth screens
│   │   ├── dashboard/            # Dashboard screen
│   │   └── theme/                # App theme
│   ├── viewmodel/
│   │   └── AuthViewModel.kt      # Auth logic
│   └── navigation/
│       └── AppNavigation.kt      # Navigation setup
```

## Notes

- The mobile app requires the web app to be running
- Make sure both apps use the same authentication system
- No Supabase configuration needed!
- Update `BASE_URL` in `ApiClient.kt` for your environment
