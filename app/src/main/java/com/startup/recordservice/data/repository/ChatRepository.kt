package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "ChatRepository"
    }
    
    suspend fun getClientConversations(clientPhone: String): Result<List<ChatConversation>> {
        return try {
            val response = apiService.getClientConversations(clientPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to load conversations (HTTP ${response.code()})"
                Log.e(TAG, "getClientConversations failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading client conversations: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getVendorConversations(businessId: String): Result<List<ChatConversation>> {
        return try {
            val response = apiService.getVendorConversations(businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to load conversations (HTTP ${response.code()})"
                Log.e(TAG, "getVendorConversations failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading vendor conversations: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getConversationMessages(conversationId: String): Result<List<ChatMessage>> {
        return try {
            val response = apiService.getConversationMessages(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to load messages (HTTP ${response.code()})"
                Log.e(TAG, "getConversationMessages failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading messages: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createConversation(clientPhone: String, businessId: String): Result<ChatConversation> {
        return try {
            val request = CreateConversationRequest(
                clientPhone = clientPhone,
                businessId = businessId
            )
            val response = apiService.createConversation(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to create conversation (HTTP ${response.code()})"
                Log.e(TAG, "createConversation failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating conversation: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(conversationId: String, message: String, clientPhone: String?, businessId: String?): Result<ChatMessage> {
        return try {
            val request = SendMessageRequest(
                conversationId = conversationId,
                message = message,
                clientPhone = clientPhone,
                businessId = businessId
            )
            val response = apiService.sendMessage(conversationId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to send message (HTTP ${response.code()})"
                Log.e(TAG, "sendMessage failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun markConversationAsRead(conversationId: String): Result<Unit> {
        return try {
            val response = apiService.markConversationAsRead(conversationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to mark as read (HTTP ${response.code()})"
                Log.e(TAG, "markConversationAsRead failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking conversation as read: ${e.message}", e)
            Result.failure(e)
        }
    }
}
