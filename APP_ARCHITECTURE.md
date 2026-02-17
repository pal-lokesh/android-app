# Android App Architecture

## Overview
This Android app is built with **Kotlin** and **Jetpack Compose**, following **MVVM architecture** and **Material 3** design principles. It connects to the **Startup-project** backend via **Retrofit** and displays data fetched on startup.

## Architecture Components

### 1. **MVVM Architecture**
- **Models**: Data classes in `data/model/` package
- **Views**: Compose screens in `ui/screens/` package
- **ViewModels**: State management in `ui/viewmodel/` package
- **Repositories**: Data access layer in `data/repository/` package

### 2. **Data Flow**
```
Backend API (Startup-project)
    ↓
Retrofit (ApiService)
    ↓
Repository (BusinessRepository, ThemeRepository, etc.)
    ↓
ViewModel (StateFlow)
    ↓
Compose UI (collectAsStateWithLifecycle)
```

### 3. **State Management**
- Uses **StateFlow** for reactive state management
- Sealed classes for UI states: `Loading`, `Success`, `Error`
- In-memory only (no database, StateFlow holds data)

### 4. **Startup Data Fetching**
- `StartupViewModel` fetches data on app startup (if logged in)
- Fetches in parallel: Businesses, Themes, Inventory
- Data available immediately when user navigates to screens

### 5. **UI Design (Matching startup-ui)**
- **Material 3** components
- **Gradient colors** matching startup-ui:
  - Login: `#667eea` → `#764ba2` → `#f093fb`
  - Signup: `#f093fb` → `#f5576c` → `#4facfe`
- **Responsive layouts** with proper spacing
- **Loading states**: CircularProgressIndicator
- **Error states**: Error icon + message + Retry button
- **Empty states**: Icon + message

## Key Features

### ✅ Implemented
1. **Authentication**
   - Login/Signup screens with gradient backgrounds
   - JWT token management (EncryptedSharedPreferences)
   - Role-based navigation (CLIENT/VENDOR)

2. **Data Fetching on Startup**
   - `StartupViewModel` pre-fetches data
   - Parallel API calls for better performance
   - Data cached in StateFlow (in-memory)

3. **Client Dashboard**
   - Displays recent orders
   - Shows available businesses
   - Click to view business details
   - Pull-to-refresh functionality

4. **Client Explore Screen**
   - Tabs: Businesses, Themes, Inventory
   - Search functionality
   - Category filtering
   - Real-time data from backend

5. **Business Detail Screen**
   - Tabs: Info, Plates, Inventory
   - Shows all vendor options
   - Displays plates with dishes
   - Inventory items with quantities

6. **Vendor Dashboard**
   - Shows vendor's businesses
   - Recent orders
   - Business management

### 🔨 In Progress / To Do
1. Cart functionality
2. Order creation flow
3. Chat system
4. Notifications
5. Ratings system

## File Structure
```
android-app/
├── app/src/main/java/com/startup/recordservice/
│   ├── data/
│   │   ├── api/              # Retrofit API interfaces
│   │   ├── local/            # TokenManager (EncryptedSharedPreferences)
│   │   ├── model/            # Data classes
│   │   └── repository/       # Repository pattern
│   ├── di/                   # Hilt dependency injection
│   ├── ui/
│   │   ├── navigation/       # Navigation graph
│   │   ├── screens/          # Compose screens
│   │   ├   ├── auth/         # Login, Signup
│   │   │   ├── client/       # Client screens
│   │   │   └── vendor/      # Vendor screens
│   │   ├── theme/            # Material 3 theme
│   │   └── viewmodel/        # ViewModels
│   └── MainActivity.kt
```

## Dependencies
- **Jetpack Compose**: UI framework
- **Material 3**: Design system
- **Hilt**: Dependency injection
- **Retrofit**: REST API client
- **OkHttp**: HTTP client with interceptors
- **Coroutines**: Async operations
- **StateFlow**: Reactive state management
- **Navigation Compose**: Navigation
- **EncryptedSharedPreferences**: Secure token storage

## Data Flow Example

### Fetching Businesses on Startup:
1. `AppNavigation` creates `StartupViewModel`
2. `StartupViewModel.init` calls `loadStartupData()`
3. `BusinessRepository.getAllBusinesses()` makes API call
4. Data stored in `StateFlow<List<BusinessResponse>>`
5. UI screens observe StateFlow via `collectAsStateWithLifecycle()`
6. Compose recomposes when data changes

## Loading & Error States

### Loading State:
```kotlin
when (uiState) {
    is UiState.Loading -> {
        CircularProgressIndicator()
    }
}
```

### Error State:
```kotlin
is UiState.Error -> {
    Column {
        Icon(Icons.Default.Error)
        Text(error.message)
        Button(onClick = { viewModel.refresh() }) {
            Text("Retry")
        }
    }
}
```

### Success State:
```kotlin
is UiState.Success -> {
    LazyColumn {
        items(data) { item ->
            ItemCard(item)
        }
    }
}
```

## In-Memory Storage
- **No database** - all data stored in StateFlow
- **Token storage**: EncryptedSharedPreferences (secure)
- **State persistence**: ViewModel survives configuration changes
- **Data refresh**: Pull-to-refresh or manual refresh

## Backend Integration
- **Base URL**: Configured in `BuildConfig.BASE_URL`
- **Authentication**: JWT token in `Authorization: Bearer <token>` header
- **Error handling**: 401 → Auto logout, network errors → User-friendly messages
- **API endpoints**: Matches Startup-project REST API structure
