package com.startup.recordservice.data.api

import com.startup.recordservice.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login/signup endpoints
        val url = originalRequest.url.toString()
        if (url.contains("/auth/signin") || url.contains("/auth/signup") || 
            url.contains("/auth/reset-password") || url.contains("/auth/check-")) {
            return chain.proceed(originalRequest)
        }
        
        val token = tokenManager.getToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }
        
        val response = chain.proceed(newRequest)
        
        // Handle 401 Unauthorized - token expired or invalid
        if (response.code == 401) {
            tokenManager.clear()
            // The app will handle logout in the ViewModel
        }
        
        return response
    }
}
