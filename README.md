# Agro Krishi - Agricultural App

A comprehensive agricultural application built with Kotlin for Android, designed to help farmers with products, tips, and weather information.

## Features

### 🔐 Authentication
- **Login/Register**: Secure user authentication with SharedPreferences
- **User Profile**: View and manage user information

### 🏠 Home Dashboard
- **Weather Integration**: Real-time weather data using location services
- **Quick Actions**: Easy access to products and farming tips
- **Farm Insights**: Statistics and user activity overview

### 🛒 Products Section
- **Product Catalog**: Browse agricultural products (Seeds, Fertilizers, Pesticides, Tools)
- **Category Filtering**: Filter products by category
- **Product Details**: Detailed product information
- **Room Database**: Local storage for products

### 💡 Farming Tips
- **Expert Tips**: Curated farming advice and best practices
- **Categories**: Tips organized by topics (Irrigation, Fertilizers, Pest Control, etc.)
- **Bookmarking**: Save favorite tips for later reference

### 🧭 Navigation
- **Bottom Navigation**: Easy access to all main sections
- **Consistent UI**: Agro-themed design with green color scheme

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Networking**: Retrofit + Gson
- **Location**: Google Play Services Location
- **UI**: Material Design 3 with custom agro theme
- **Navigation**: Fragment-based with Bottom Navigation

## App Structure

```
├── Authentication
│   ├── LoginActivity
│   └── RegisterActivity
├── Main Navigation
│   ├── HomeActivity (Container)
│   ├── HomeFragment
│   └── ProfileFragment
├── Features
│   ├── ProductsActivity
│   ├── ProductDetailsActivity
│   ├── TipsActivity
│   └── TipDetailsActivity
└── Data Layer
    ├── Room Database
    ├── Retrofit API
    └── SharedPreferences
```

## Color Scheme (Agro Theme)

- **Primary Green**: #4CAF50
- **Primary Green Dark**: #388E3C
- **Primary Green Light**: #C8E6C9
- **Accent Orange**: #FF9800
- **Background Light**: #F8F9FA
- **Surface White**: #FFFFFF

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run the app on device/emulator

## API Integration

- **Weather API**: OpenWeatherMap integration for real-time weather data
- **Location Services**: GPS-based location detection for weather

## Database Schema

- **Products**: Local Room database with categories and details
- **User Data**: SharedPreferences for user authentication and preferences

## Future Enhancements

- Order management system
- Push notifications for weather alerts
- Offline mode improvements
- Social features for farmers
- Marketplace integration

---

**Built with ❤️ for farmers by the Agro Krishi team**

## 👨‍💼 Admin Panel

### Admin Authentication
- **Secure Admin Login**: Firebase Authentication with admin role verification
- **Admin Access Control**: Only predefined admin emails can access the panel
- **Session Management**: Secure admin session handling

### Admin Dashboard
- **User Statistics**: View total users, active users, and analytics
- **Quick Actions**: Access to user management, reports, and system settings
- **Real-time Data**: Live statistics from Firebase Firestore

### Admin Features
- **User Management**: View and manage user accounts (coming soon)
- **Analytics & Reports**: Usage statistics and reports (coming soon)  
- **System Settings**: Configure app settings (coming soon)

### Admin Access
1. **From Login Screen**: Click "Admin Access" link
2. **Admin Credentials**: Use predefined admin accounts:
   - **Email**: `admin@agrokrishiseva.com` | **Password**: `admin123`
   - **Email**: `superadmin@agrokrishiseva.com` | **Password**: `super123`
3. **Quick Demo**: Use "Quick Fill (Demo)" button for instant credential filling
4. **Auto-Creation**: Admin accounts are automatically created in Firebase if they don't exist

### Admin Configuration
To add new admin users:
1. Add email to `AdminManager.ADMIN_EMAILS` set
2. Or create document in Firestore `admins` collection:
   ```json
   {
     "email": "newadmin@example.com",
     "isActive": true
   }
   ```