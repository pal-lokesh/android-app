# Android App Connection Troubleshooting Guide

## Problem: Android app not picking up data from backend

### Quick Checks

1. **Is the backend running?**
   ```bash
   # Check if backend is running on port 8080
   # Windows:
   netstat -an | findstr :8080
   # Mac/Linux:
   lsof -i :8080
   ```

2. **Check Logcat for errors**
   - Open Android Studio
   - View → Tool Windows → Logcat
   - Filter by tag: `AuthRepository` or `NetworkModule`
   - Look for connection errors

### Configuration Based on Device Type

#### For Android Emulator

The app is configured to use: `http://10.0.2.2:8080/api/`

**10.0.2.2** is the special IP address that the Android emulator uses to access your host machine's localhost.

**Steps:**
1. Ensure backend is running on `localhost:8080`
2. No additional configuration needed
3. Test connection by trying to login

#### For Physical Android Device

You need to use your computer's actual IP address instead of `10.0.2.2`.

**Steps:**

1. **Find your computer's IP address:**
   - **Windows**: Open Command Prompt → `ipconfig` → Look for "IPv4 Address"
   - **Mac/Linux**: Open Terminal → `ifconfig` or `ip addr` → Look for inet address

2. **Update `app/build.gradle.kts`:**
   ```kotlin
   buildTypes {
       debug {
           isMinifyEnabled = false
           // Replace YOUR_IP with your actual IP (e.g., "192.168.1.100")
           buildConfigField("String", "BASE_URL", "\"http://YOUR_IP:8080/api/\"")
           buildConfigField("boolean", "IS_DEBUG", "true")
       }
   }
   ```

3. **Ensure phone and computer are on the same Wi-Fi network**

4. **Check backend CORS settings:**
   - Backend should allow connections from your device's IP
   - Check `application.properties` or `SecurityConfig.java`

5. **Rebuild the app:**
   - Build → Clean Project
   - Build → Rebuild Project

### Common Error Messages and Solutions

#### Error: "Cannot connect to server"
**Causes:**
- Backend not running
- Wrong IP address
- Firewall blocking connection

**Solutions:**
1. Start backend: `cd startup-project && mvn spring-boot:run`
2. Verify backend is accessible: Open browser → `http://localhost:8080/api/businesses`
3. Check firewall settings (Windows Firewall, antivirus)

#### Error: "Connection refused"
**Causes:**
- Backend not running
- Backend running on different port
- Backend not accepting connections from device IP

**Solutions:**
1. Check backend is running: `netstat -an | findstr :8080`
2. Verify backend port in `application.properties`
3. Check backend `SecurityConfig.java` allows your origin

#### Error: "Request timeout"
**Causes:**
- Slow network
- Backend taking too long to respond
- Network connectivity issues

**Solutions:**
1. Check network connection
2. Try from browser first to verify backend is responsive
3. Increase timeout in `NetworkModule.kt` if needed

### Testing Connection

#### Method 1: Check Logcat
1. Open Android Studio
2. Run app on device/emulator
3. Open Logcat (View → Tool Windows → Logcat)
4. Filter by: `AuthRepository`
5. Try to login
6. Check logs for:
   - "Attempting login for phone: ..."
   - "Login response code: ..."
   - Any error messages

#### Method 2: Test Backend Directly
1. Open browser on your computer
2. Navigate to: `http://localhost:8080/api/businesses`
3. If this works, backend is running correctly
4. If using physical device, try: `http://YOUR_IP:8080/api/businesses` from phone's browser

#### Method 3: Check Network Configuration
1. Verify `BuildConfig.BASE_URL` is correct:
   - Add temporary log: `Log.d("Network", "Base URL: ${BuildConfig.BASE_URL}")`
   - Check Logcat output

### Backend Configuration Check

#### Verify CORS Settings

Check `startup-project/src/main/java/com/example/RecordService/config/SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",  // React frontend
        "http://10.0.2.2:8080",   // Android emulator
        "http://YOUR_IP:8080"     // Add your device IP if needed
    ));
    // ... rest of config
}
```

#### Verify Backend is Accessible

Test from command line:
```bash
# Test if backend responds
curl http://localhost:8080/api/businesses

# Or from phone browser (if on same network)
# Open: http://YOUR_COMPUTER_IP:8080/api/businesses
```

### Step-by-Step Debugging

1. **Verify Backend is Running**
   ```bash
   cd startup-project
   mvn spring-boot:run
   # Should see: "Started RecordServiceApplication"
   ```

2. **Test Backend from Browser**
   - Open: `http://localhost:8080/api/businesses`
   - Should return JSON data or empty array

3. **Check Android App Configuration**
   - Open `app/build.gradle.kts`
   - Verify `BASE_URL` is correct for your setup
   - For emulator: `http://10.0.2.2:8080/api/`
   - For device: `http://YOUR_IP:8080/api/`

4. **Check Logcat**
   - Run app
   - Try to login
   - Check Logcat for error messages
   - Look for "AuthRepository" tag

5. **Verify Network Permissions**
   - Check `AndroidManifest.xml` has:
     ```xml
     <uses-permission android:name="android.permission.INTERNET" />
     <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     ```

6. **Check Cleartext Traffic**
   - `AndroidManifest.xml` should have:
     ```xml
     android:usesCleartextTraffic="true"
     ```
   - This allows HTTP (non-HTTPS) connections

### Quick Fixes

#### Fix 1: Update Base URL for Physical Device
```kotlin
// In app/build.gradle.kts
buildConfigField("String", "BASE_URL", "\"http://192.168.1.100:8080/api/\"")
// Replace 192.168.1.100 with your actual IP
```

#### Fix 2: Add Your IP to Backend CORS
```java
// In SecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://10.0.2.2:8080",
    "http://192.168.1.100:8080"  // Add your device IP
));
```

#### Fix 3: Verify Backend Port
```properties
# In application.properties
server.port=8080
```

### Still Not Working?

1. **Check Logcat for detailed error messages**
2. **Verify backend logs** (check console where backend is running)
3. **Test with Postman/curl** to verify backend endpoints work
4. **Try React frontend** to verify backend is accessible
5. **Check firewall/antivirus** isn't blocking connections

### Example Logcat Output (Success)
```
D/AuthRepository: Attempting login for phone: 1234567890
D/AuthRepository: Login response code: 200
D/AuthRepository: Login response isSuccessful: true
D/AuthRepository: Login successful for user: 1234567890
```

### Example Logcat Output (Error)
```
D/AuthRepository: Attempting login for phone: 1234567890
E/AuthRepository: Network error: Unable to resolve host "10.0.2.2": No address associated with hostname
E/AuthRepository: Cannot connect to server...
```

---

**Need more help?** Check the logs in Logcat and share the error messages.
