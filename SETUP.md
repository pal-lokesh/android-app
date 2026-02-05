# Android Application Setup Guide

## Prerequisites

1. **Android Studio** (Hedgehog | 2023.1.1 or later)
2. **JDK 17** or later
3. **Android SDK** (API 24 or higher)
4. **Backend Server** running on `http://localhost:8080`

## Quick Start

### 1. Open Project in Android Studio

```bash
# Navigate to the android-app directory
cd android-app

# Open in Android Studio
# File -> Open -> Select android-app folder
```

### 2. Configure Backend URL

The app is pre-configured to connect to `http://10.0.2.2:8080/api/` which is the Android emulator's way of accessing `localhost:8080`.

**For Physical Device Testing:**
1. Find your computer's IP address:
   - Windows: `ipconfig` (look for IPv4 Address)
   - Mac/Linux: `ifconfig` or `ip addr`
2. Update `BASE_URL` in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://YOUR_IP_ADDRESS:8080/api/\"")
   ```
3. Ensure your phone and computer are on the same network
4. Ensure your backend allows connections from your IP

### 3. Sync Gradle

Android Studio will prompt you to sync Gradle files. Click "Sync Now" and wait for dependencies to download.

### 4. Run the Application

1. Connect an Android device via USB (with USB debugging enabled) OR start an Android emulator
2. Click the "Run" button (green play icon) or press `Shift+F10`
3. Select your device/emulator
4. The app will build and install automatically

## Project Structure

```
android-app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/startup/recordservice/
│   │       │   ├── data/           # Data layer
│   │       │   │   ├── api/        # Retrofit API services
│   │       │   │   ├── model/      # Data models
│   │       │   │   ├── repository/ # Repository classes
│   │       │   │   └── local/      # Local storage (DataStore)
│   │       │   ├── di/             # Dependency injection
│   │       │   ├── ui/             # UI layer
│   │       │   │   ├── screens/    # Compose screens
│   │       │   │   ├── theme/      # Material Design theme
│   │       │   │   ├── viewmodel/  # ViewModels
│   │       │   │   └── navigation/ # Navigation
│   │       │   └── MainActivity.kt
│   │       └── res/                # Resources
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Features Implemented

✅ **Authentication**
- Login screen
- Signup screen (basic - OTP verification to be added)
- JWT token management
- Auto-login on app restart

✅ **Navigation**
- Bottom navigation for clients
- Bottom navigation for vendors
- Screen routing based on user type

✅ **UI Components**
- Material Design 3 components
- Custom theme
- Responsive layouts

## Features To Be Implemented

- [ ] OTP verification in signup flow
- [ ] Business listing and details
- [ ] Cart functionality
- [ ] Order creation and tracking
- [ ] Inventory/Plate/Dish management (vendor)
- [ ] Notifications
- [ ] Chat functionality
- [ ] Image loading and display
- [ ] Profile management
- [ ] Order history

## Troubleshooting

### Build Errors

1. **Gradle Sync Failed**
   - Check internet connection
   - Invalidate caches: File -> Invalidate Caches / Restart
   - Clean project: Build -> Clean Project
   - Rebuild: Build -> Rebuild Project

2. **Dependency Resolution Failed**
   - Check `build.gradle.kts` files
   - Ensure you're using the correct Gradle version
   - Try: File -> Sync Project with Gradle Files

### Runtime Errors

1. **Network Error / Cannot Connect to Backend**
   - Verify backend is running on port 8080
   - Check BASE_URL in `app/build.gradle.kts`
   - For physical device: Ensure correct IP address
   - Check AndroidManifest.xml has INTERNET permission

2. **App Crashes on Launch**
   - Check Logcat for error messages
   - Verify all dependencies are synced
   - Clear app data: Settings -> Apps -> Record Service -> Clear Data

### API Connection Issues

- **Emulator**: Use `http://10.0.2.2:8080/api/`
- **Physical Device**: Use your computer's IP (e.g., `http://192.168.1.100:8080/api/`)
- Ensure backend CORS allows your origin
- Check backend logs for incoming requests

## Development Tips

1. **View Logs**: Use Logcat in Android Studio (View -> Tool Windows -> Logcat)
2. **Debug Network**: Enable logging interceptor (already configured in RetrofitClient)
3. **Test API**: Use Postman or similar to test backend endpoints first
4. **State Management**: All UI state is managed through ViewModels
5. **Navigation**: Use Navigation Compose for screen transitions

## Next Steps

1. Implement OTP verification flow
2. Create business detail screens
3. Implement cart state management
4. Add image loading with Coil
5. Implement order creation flow
6. Add vendor management screens

## Support

For issues or questions:
1. Check the main README.md
2. Review backend API documentation
3. Check Android Studio Logcat for detailed error messages
