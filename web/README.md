# Trinity HRM - Web App

A modern HR Management System built with Next.js, TypeScript, and Tailwind CSS.

## Features

- 🔐 **Local Authentication** - JWT-based auth with secure password hashing
- ☁️ **JSONBin.io Integration** - Cloud storage shared with mobile apps
- 👥 **Role-Based Access Control** - Admin, Department Head, and Employee roles
- 📱 **Cross-Platform** - Shares data with React Native and Kotlin mobile apps
- 🎨 **Modern UI** - Beautiful, responsive design with Tailwind CSS

## Quick Start

### Prerequisites
- Node.js 18+ and npm
- JSONBin.io account (free)

### Setup

1. **Clone and install:**
   ```bash
   cd web
   npm install
   ```

2. **Create `.env`:**
   ```env
   JSONBIN_API_KEY=your_jsonbin_master_key_here
   AUTH_SECRET=your-secret-key-min-32-chars-long
   ```

3. **Get JSONBin API Key:**
   - Sign up at https://jsonbin.io/
   - Get your Master Key from dashboard
   - Add to `.env`

4. **Run development server:**
   ```bash
   npm run dev
   ```

5. **Open browser:**
   - http://localhost:3000

## Documentation

- [Local Setup Guide](./LOCAL_SETUP.md) - Detailed setup instructions
- [Role-Based Access Control](../ROLE_BASED_ACCESS_CONTROL.md) - RBAC documentation

## Tech Stack

- **Framework**: Next.js 14
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **Authentication**: JWT (jose)
- **Storage**: JSONBin.io
- **State**: Zustand

## Project Structure

```
web/
├── app/                # Next.js app directory
│   ├── api/           # API routes
│   ├── dashboard/     # Dashboard pages
│   └── (auth)/        # Auth pages
├── components/        # React components
├── lib/              # Utilities
│   ├── auth/         # Authentication
│   └── storage/       # JSONBin.io integration
└── data/             # Local data (auto-created)
```

## Shared Database

All three apps share the same JSONBin.io database:
- ✅ Web app (this app)
- ✅ React Native mobile app
- ✅ Kotlin Android app

## Development

```bash
# Development
npm run dev

# Production build
npm run build
npm start

# Lint
npm run lint
```

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JSONBIN_API_KEY` | Yes | JSONBin.io Master Key |
| `AUTH_SECRET` | Yes | JWT secret (min 32 chars) |
| `JSONBIN_BIN_ID` | No | Existing bin ID (auto-created) |

## License

Private project
