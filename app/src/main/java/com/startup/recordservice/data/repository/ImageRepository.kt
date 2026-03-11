package com.startup.recordservice.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.FileUploadResponse
import com.startup.recordservice.data.model.ImageCreateRequest
import com.startup.recordservice.data.model.ImageResponse
import com.startup.recordservice.data.model.ThemeImageCreateRequest
import com.startup.recordservice.data.model.ThemeImageResponse
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class ImageRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "ImageRepository"
    }

    suspend fun createThemeImage(
        request: ThemeImageCreateRequest,
        vendorPhone: String
    ): Result<ThemeImageResponse> {
        return try {
            Log.d(TAG, "Creating theme image for themeId=${request.themeId}")
            val response = apiService.createThemeImage(request, vendorPhone)

            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                Log.d(TAG, "Theme image created with id=${created.imageId}")
                Result.success(created)
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                        ?: "Failed to create theme image (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create theme image (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to create theme image: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating theme image: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getImagesByThemeId(themeId: String): Result<List<ImageResponse>> {
        return try {
            val response = apiService.getImagesByThemeId(themeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to fetch images (HTTP ${response.code()})"
                Log.e(TAG, "Failed to fetch theme images: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching theme images: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createImage(request: ImageCreateRequest): Result<ImageResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            Log.d(TAG, "Creating image record: ${request.imageName}")
            val response = apiService.createImage(request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "Failed to create image record (HTTP ${response.code()})"
                Log.e(TAG, "Failed to create image record: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image record: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        category: String,
        itemId: String
    ): Result<String> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Failed to open image stream"))
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val requestFile: RequestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                uri.lastPathSegment ?: "upload.jpg",
                requestFile
            )

            val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
            val itemIdPart = itemId.toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d(TAG, "Uploading file for category=$category, itemId=$itemId, mimeType=$mimeType")
            val response = apiService.uploadFile(filePart, categoryPart, itemIdPart)

            if (response.isSuccessful && response.body()?.success == true) {
                val body: FileUploadResponse = response.body()!!
                val filePath = body.filePath
                if (filePath != null) {
                    Log.d(TAG, "File uploaded successfully: $filePath")
                    Result.success(filePath)
                } else {
                    Result.failure(Exception("File upload succeeded but no filePath returned"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                    ?: "File upload failed (HTTP ${response.code()})"
                Log.e(TAG, "File upload failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file: ${e.message}", e)
            Result.failure(e)
        }
    }
}

