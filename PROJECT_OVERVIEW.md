# Complete Project Overview

This document provides a comprehensive overview of all three projects: **Android App**, **Spring Boot Backend**, and **React Frontend**.

## Project Structure

### 1. Android App (`android-app/`)
- **Technology**: Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit
- **Status**: ✅ Core authentication implemented, models aligned with backend
- **Location**: `C:\Users\jitendra\Documents\android-app`

### 2. Spring Boot Backend (`startup-project/`)
- **Technology**: Java, Spring Boot, PostgreSQL, JWT
- **Status**: ✅ Production-ready, all endpoints functional
- **Location**: `C:\Users\jitendra\Documents\startup-project`

### 3. React Frontend (`Startup-ui/`)
- **Technology**: React, TypeScript, Material-UI
- **Status**: ✅ Full-featured web application
- **Location**: `C:\Users\jitendra\Documents\Startup-ui`

---

## Android App Architecture

### Current Implementation Status

#### ✅ Completed
- **Authentication System**
  - Login/Signup screens
  - JWT token management
  - Secure token storage (EncryptedSharedPreferences)
  - Auto-login on app restart
  - 401 error handling (auto-logout)

- **Data Models**
  - Auth models (LoginRequest, LoginResponse, SignupRequest, SignupResponse)
  - User models (UserResponse)
  - Order models (OrderRequest, OrderResponse, OrderItemRequest, OrderItemResponse) - **FIXED to match backend**
  - Business, Plate, Dish, Inventory, Notification, Rating models

- **Networking**
  - Retrofit API service
  - JWT AuthInterceptor
  - NetworkModule (Hilt)
  - Base URL configuration (BuildConfig)

- **Navigation**
  - NavGraph with role-based routing
  - Login → Signup navigation
  - Role-based dashboard routing (Client/Vendor)

- **UI Components**
  - LoginScreen
  - SignupScreen
  - ClientDashboardScreen (placeholder)
  - VendorDashboardScreen (placeholder)
  - SplashScreen
  - Theme system (Light/Dark mode)

#### ⚠️ Needs Implementation
- **Order Management**
  - Create order flow
  - View orders list
  - Order details screen
  - Order tracking

- **Client Features**
  - Explore businesses/plates/dishes
  - Shopping cart
  - Checkout flow
  - Order history
  - Profile management

- **Vendor Features**
  - Business management
  - Inventory management
  - Plate/Dish management
  - Order management (view, update status, adjust amount)
  - Analytics dashboard

- **Additional Features**
  - Notifications
  - Ratings/Reviews
  - Chat/Messaging
  - Image uploads
  - Location services

---

## Backend API Endpoints

### Authentication (`/api/auth`)
- `POST /signin` - Login
- `POST /signup` - Register
- `GET /check-phone/{phoneNumber}` - Check phone availability
- `GET /check-email/{email}` - Check email availability
- `POST /reset-password` - Reset password (phone/email OTP)
- `POST /change-password` - Change password (authenticated)

### Orders (`/api/orders`)
- `POST /` - Create order
- `GET /user/{userId}` - Get user orders
- `GET /{orderId}` - Get order details
- `PUT /{orderId}/status` - Update order status
- `PUT /{orderId}/amount` - Adjust order amount
- `GET /business/{businessId}` - Get business orders

### Business (`/api/businesses`)
- `GET /` - Get all businesses
- `GET /{businessId}` - Get business details
- `GET /user/{phoneNumber}` - Get user's businesses

### Plates (`/api/plates`)
- `GET /business/{businessId}` - Get business plates
- `GET /{plateId}` - Get plate details

### Dishes (`/api/dishes`)
- `GET /plate/{plateId}` - Get plate dishes

### Inventory (`/api/inventory`)
- `GET /business/{businessId}` - Get business inventory

### Users (`/api/users`)
- `GET /{phoneNumber}` - Get user details
- `DELETE /{phoneNumber}` - Delete user account

### Notifications (`/api/notifications`)
- `GET /client/{phoneNumber}` - Get client notifications
- `GET /vendor/{businessId}` - Get vendor notifications

### Ratings (`/api/ratings`)
- `GET /client/{phoneNumber}` - Get client ratings
- `POST /` - Create rating

---

## Data Model Alignment

### OrderRequest (Backend vs Android)

#### Backend (Java)
```java
public class OrderRequest {
    private String userId;
    private List<OrderItemRequest> items;
    private String customerName;        // REQUIRED
    private String customerEmail;       // REQUIRED
    private String customerPhone;       // REQUIRED
    private String deliveryAddress;      // REQUIRED
    private Double deliveryLatitude;     // Optional
    private Double deliveryLongitude;    // Optional
    private String deliveryDate;         // REQUIRED
    private String specialNotes;         // Optional
}
```

#### Android (Kotlin) - ✅ FIXED
```kotlin
data class OrderRequest(
    val userId: String,
    val items: List<OrderItemRequest>,
    val customerName: String,           // ✅ Added
    val customerEmail: String,          // ✅ Added
    val customerPhone: String,          // ✅ Added
    val deliveryAddress: String,        // ✅ Required (was optional)
    val deliveryDate: String,           // ✅ Added
    val deliveryLatitude: Double? = null,    // ✅ Added
    val deliveryLongitude: Double? = null,   // ✅ Added
    val specialNotes: String? = null        // ✅ Fixed name (was specialInstructions)
)
```

### OrderItemRequest (Backend vs Android)

#### Backend (Java)
```java
public class OrderItemRequest {
    private String itemId;
    private String itemName;            // REQUIRED
    private Double itemPrice;            // REQUIRED
    private Integer quantity;
    private String itemType;            // "theme", "inventory", "plate"
    private String businessId;
    private String businessName;        // Optional
    private String imageUrl;            // Optional
    private LocalDate bookingDate;      // Optional (for plates)
    private String selectedDishes;      // JSON string (not List)
}
```

#### Android (Kotlin) - ✅ FIXED
```kotlin
data class OrderItemRequest(
    val itemId: String,
    val itemName: String,               // ✅ Added
    val itemPrice: Double,              // ✅ Added
    val quantity: Int,
    val itemType: String,               // ✅ Fixed values
    val businessId: String,
    val businessName: String? = null,   // ✅ Added
    val imageUrl: String? = null,       // ✅ Added
    val bookingDate: String? = null,   // ✅ Format: "yyyy-MM-dd"
    val selectedDishes: String? = null  // ✅ Fixed: String (was List<String>)
)
```

---

## Configuration

### Android App Base URL

**For Android Emulator:**
```kotlin
BASE_URL = "http://10.0.2.2:8080/api/"
```
- `10.0.2.2` is the emulator's special IP for accessing host machine's localhost

**For Physical Device:**
```kotlin
BASE_URL = "http://YOUR_COMPUTER_IP:8080/api/"
```
- Replace `YOUR_COMPUTER_IP` with your computer's local IP address
- Ensure phone and computer are on the same network
- Ensure backend allows connections from your IP

**For Production:**
```kotlin
BASE_URL = "https://your-production-url.com/api/"
```

### React Frontend Base URL

**Development:**
```typescript
API_URL = "http://localhost:8080/api"
```

**Production:**
```typescript
API_URL = process.env.REACT_APP_API_URL
```

---

## Key Differences: Android vs React Frontend

### Authentication Flow

**Android:**
1. User enters phone/password
2. Retrofit calls `/api/auth/signin`
3. Token saved in EncryptedSharedPreferences
4. Auto-login on app restart
5. 401 errors trigger auto-logout

**React:**
1. User enters phone/password
2. Axios calls `/api/auth/signin`
3. Token saved in localStorage
4. Context API manages auth state
5. Manual logout on 401

### State Management

**Android:**
- ViewModels with StateFlow
- Repository pattern
- Hilt dependency injection

**React:**
- Context API (AuthContext, CartContext, etc.)
- React hooks (useState, useEffect)
- Service layer for API calls

### Navigation

**Android:**
- Jetpack Navigation Compose
- Type-safe navigation with sealed classes
- Deep linking support

**React:**
- React Router
- Route-based navigation
- Protected routes

---

## Known Issues & Fixes

### ✅ Fixed Issues

1. **UserResponse Redeclaration**
   - **Problem**: `UserResponse` defined in both `AuthModels.kt` and `UserModels.kt`
   - **Fix**: Removed duplicate from `AuthModels.kt`, kept in `UserModels.kt`

2. **OrderRequest Model Mismatch**
   - **Problem**: Android model missing required fields (customerName, customerEmail, customerPhone, deliveryDate)
   - **Fix**: Updated `OrderRequest` to match backend exactly

3. **OrderItemRequest Model Mismatch**
   - **Problem**: Missing fields (itemName, itemPrice, businessName, imageUrl) and wrong type for selectedDishes
   - **Fix**: Updated to match backend, changed selectedDishes from `List<String>` to `String` (JSON)

### ⚠️ Backend Warnings (Non-Critical)

- Null safety warnings in test files
- Deprecated MockBean usage
- Unused imports
- These don't affect functionality

---

## Next Steps for Android App

### Priority 1: Core Order Flow
1. **Shopping Cart**
   - Add items to cart
   - View cart
   - Update quantities
   - Remove items

2. **Checkout Flow**
   - Customer information form
   - Delivery address input
   - Delivery date picker
   - Order summary
   - Create order API call

3. **Order Management**
   - Order list screen
   - Order details screen
   - Order status tracking

### Priority 2: Client Features
1. **Explore Screen**
   - Business list
   - Plate/Dish browsing
   - Search and filters
   - Business details

2. **Profile Management**
   - View profile
   - Edit profile
   - Change password
   - Delete account

### Priority 3: Vendor Features
1. **Business Dashboard**
   - Order management
   - Inventory management
   - Analytics

2. **Product Management**
   - Create/Edit plates
   - Create/Edit dishes
   - Manage inventory

### Priority 4: Additional Features
1. **Notifications**
   - Push notifications
   - In-app notifications

2. **Ratings & Reviews**
   - View ratings
   - Submit ratings

3. **Chat/Messaging**
   - Client-Vendor chat

---

## Testing Checklist

### Android App
- [ ] Login with valid credentials
- [ ] Login with invalid credentials (error handling)
- [ ] Signup flow
- [ ] Auto-login on app restart
- [ ] Logout functionality
- [ ] 401 error handling (auto-logout)
- [ ] Network error handling
- [ ] Role-based navigation (Client vs Vendor)

### Backend Integration
- [ ] All API endpoints accessible
- [ ] JWT token authentication working
- [ ] Order creation with correct data format
- [ ] Error responses handled correctly

---

## Build & Deploy

### Android App
1. **Debug Build**
   ```bash
   ./gradlew assembleDebug
   ```
   - APK: `app/build/outputs/apk/debug/app-debug.apk`

2. **Release Build**
   ```bash
   ./gradlew assembleRelease
   ```
   - Requires signing configuration
   - APK: `app/build/outputs/apk/release/app-release.apk`

### Backend
```bash
mvn clean package
java -jar target/record-service-*.jar
```

### React Frontend
```bash
npm run build
# Deploy build/ folder to hosting service
```

---

## Support & Resources

### Documentation
- Android: `android-app/README.md`
- Backend: `startup-project/README.md`
- React: `Startup-ui/README.md`

### Key Files
- Android Gradle: `android-app/app/build.gradle.kts`
- Backend Config: `startup-project/src/main/resources/application.properties`
- React Config: `Startup-ui/src/config/apiConfig.ts`

---

**Last Updated**: Current Date
**Status**: ✅ Models aligned, ready for feature implementation
