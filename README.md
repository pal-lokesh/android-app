# Record Service Android Application

A production-ready Android application built with Kotlin, Jetpack Compose, MVVM architecture, and Hilt dependency injection. Connects to your existing Spring Boot backend.

## Features

✅ **Authentication**
- JWT-based login and signup
- Secure token storage using EncryptedSharedPreferences
- Auto-login on app restart
- Automatic logout on 401 errors

✅ **Role-Based Navigation**
- Dynamic UI based on User/Vendor role
- Separate dashboards for Clients and Vendors
- Role-specific navigation flows

✅ **Architecture**
- MVVM pattern with ViewModels
- Repository pattern for data layer
- Hilt for dependency injection
- StateFlow for reactive state management

✅ **UI/UX**
- Fully responsive (phones & tablets)
- Adaptive layouts using Compose
- Light & Dark mode support
- Loading, error, and empty states
- Material Design 3

✅ **Networking**
- Retrofit with OkHttp
- JWT token interceptor
- Network error handling
- Request/response logging (debug mode)

## Setup Instructions

### 1. Open in Android Studio

```bash
cd android-app
# Open in Android Studio: File -> Open -> Select android-app folder
```

### 2. Configure Backend URL

**For Android Emulator:**
- Already configured: `http://10.0.2.2:8080/api/`
- This is the emulator's way of accessing `localhost:8080`

**For Physical Device:**
1. Find your computer's IP address:
   - Windows: `ipconfig` → Look for IPv4 Address
   - Mac/Linux: `ifconfig` or `ip addr`
2. Update `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL_DEV", "\"http://YOUR_IP:8080/api/\"")
   ```
3. Ensure phone and computer are on the same network
4. Ensure backend allows connections from your IP

### 3. Sync Gradle

Android Studio will prompt to sync. Click "Sync Now" and wait for dependencies.

### 4. Run the Application

1. Connect Android device (USB debugging enabled) OR start emulator
2. Click Run button (green play icon) or press `Shift+F10`
3. Select your device/emulator
4. App will build and install automatically

## Project Structure

```
app/src/main/java/com/startup/recordservice/
├── data/
│   ├── api/              # Retrofit API interfaces
│   │   ├── ApiService.kt
│   │   └── AuthInterceptor.kt
│   ├── local/            # Local storage
│   │   └── TokenManager.kt (EncryptedSharedPreferences)
│   ├── model/            # Data models (DTOs)
│   └── repository/       # Repository classes
├── di/                   # Hilt modules
│   └── NetworkModule.kt
├── ui/
│   ├── screens/          # Compose screens
│   │   ├── auth/         # Login, Signup
│   │   ├── client/       # Client screens
│   │   └── vendor/       # Vendor screens
│   ├── navigation/       # Navigation setup
│   ├── theme/            # Material Design theme
│   └── viewmodel/        # ViewModels
├── RecordServiceApplication.kt
└── MainActivity.kt
```

## Key Components

### Authentication Flow

1. **Login Screen** → User enters phone/password
2. **API Call** → Retrofit calls `/api/auth/signin`
3. **Token Storage** → EncryptedSharedPreferences saves JWT token
4. **Navigation** → Based on user role (CLIENT/VENDOR)
5. **Auto-Login** → On app restart, checks stored token

### JWT Token Management

- **AuthInterceptor**: Automatically adds `Authorization: Bearer <token>` header
- **401 Handling**: Clears token and triggers logout
- **Secure Storage**: Uses Android's EncryptedSharedPreferences

### Role-Based Navigation

- **CLIENT** → ClientDashboardScreen with bottom nav (Home, Explore, Orders, Profile)
- **VENDOR** → VendorDashboardScreen with bottom nav (Dashboard, Orders, Inventory, Profile)

## Build Configuration

### Debug Build
- Base URL: `http://10.0.2.2:8080/api/` (emulator) or your IP
- Logging: Full HTTP request/response logging
- Minification: Disabled

### Release Build
- Base URL: Configure `BASE_URL_PROD` in `build.gradle.kts`
- Logging: Disabled
- Minification: Enabled with ProGuard rules

## API Integration

The app uses the same REST API endpoints as your Spring Boot backend:

- `POST /api/auth/signin` - Login
- `POST /api/auth/signup` - Signup
- `GET /api/auth/check-phone/{phoneNumber}` - Check phone availability
- `GET /api/auth/check-email/{email}` - Check email availability
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/change-password` - Change password

All other endpoints require JWT token in Authorization header.

## Error Handling

- **401 Unauthorized**: Automatically logs out user
- **Network Errors**: Displayed to user with retry option
- **Validation Errors**: Shown inline in forms
- **Loading States**: Shown during API calls

## Responsive Design

- Uses `Modifier.weight()` for flexible layouts
- Adapts to different screen sizes
- Supports tablets with multi-column layouts
- No fixed widths/heights

## Dark Mode

- Automatically follows system theme
- Can be toggled in system settings
- All components support both themes

## Generating Signed APK

1. **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Create or select keystore
4. Choose **release** build variant
5. Click **Finish**

The APK will be generated in `app/release/` directory.

## Troubleshooting

### Build Errors
- **Gradle Sync Failed**: Invalidate caches (File → Invalidate Caches / Restart)
- **Dependency Issues**: Clean project (Build → Clean Project), then rebuild

### Runtime Errors
- **Network Error**: Check BASE_URL, ensure backend is running, check network permissions
- **401 Errors**: Token expired - user will be logged out automatically
- **App Crashes**: Check Logcat for detailed error messages

### API Connection
- **Emulator**: Use `10.0.2.2:8080`
- **Physical Device**: Use your computer's IP address
- **CORS**: Ensure backend allows your origin

## Next Steps

To extend the application:

1. **Add More Screens**: Create new screens in `ui/screens/`
2. **Add ViewModels**: Create ViewModels in `ui/viewmodel/`
3. **Add API Endpoints**: Extend `ApiService.kt` with new endpoints
4. **Add Repositories**: Create repositories in `data/repository/`
5. **Update Navigation**: Add routes in `NavGraph.kt`

## Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Retrofit**: HTTP client
- **OkHttp**: HTTP interceptor
- **Coroutines**: Async programming
- **Material Design 3**: UI components
- **EncryptedSharedPreferences**: Secure storage

## Support

For issues:
1. Check Logcat for error messages
2. Verify backend is running and accessible
3. Check network configuration
4. Review API response format matches models

---

**Ready to build and deploy!** 🚀
