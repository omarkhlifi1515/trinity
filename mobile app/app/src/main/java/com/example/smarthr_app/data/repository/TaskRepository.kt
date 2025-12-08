package com.example.smarthr_app.data.repository

import android.content.Context
import android.net.Uri
import com.example.smarthr_app.data.local.DataStoreManager
import com.example.smarthr_app.data.model.*
import com.example.smarthr_app.data.remote.RetrofitInstance
import com.example.smarthr_app.utils.Resource
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class TaskRepository(private val dataStoreManager: DataStoreManager) {

    private suspend fun getToken(): String? {
        return dataStoreManager.authToken.first()
    }

    suspend fun createTask(
        context: Context,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>,
        imageUri: Uri?
    ): Resource<TaskResponse> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val priorityPart = priority.toRequestBody("text/plain".toMediaTypeOrNull())

            val employeeParts = employees.map { id ->
                MultipartBody.Part.createFormData("employees[]", id)
            }

            val imagePart = if (imageUri != null) {
                val file = getFileFromUri(context, imageUri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                } else null
            } else null

            val response = if (employees.isNotEmpty()) {
                RetrofitInstance.api.createTask(
                    "Bearer $token",
                    titlePart,
                    descriptionPart,
                    priorityPart,
                    employeeParts,
                    imagePart
                )
            } else {
                 RetrofitInstance.api.createTaskWithoutEmployees(
                    "Bearer $token",
                    titlePart,
                    descriptionPart,
                    priorityPart,
                    imagePart
                )
            }

            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to create task: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun getUserTasks(): Resource<List<TaskResponse>> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val response = RetrofitInstance.api.getUserTasks("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to fetch tasks: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun getCompanyTasks(): Resource<List<TaskResponse>> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val response = RetrofitInstance.api.getCompanyTasks("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to fetch company tasks: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun getTaskById(taskId: String): Resource<TaskResponse> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val response = RetrofitInstance.api.getTaskById("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to fetch task: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        priority: String,
        status: String,
        employees: List<String>
    ): Resource<TaskResponse> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")

            // Using the first employee as assigneeId as TaskRequest currently supports single assignee
            val assigneeId = employees.firstOrNull()?.toIntOrNull()

            val request = TaskRequest(
                title = title,
                description = description,
                priority = priority,
                dueDate = null,
                assigneeId = assigneeId
            )

            val response = RetrofitInstance.api.updateTask("Bearer $token", taskId, request)
             if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to update task: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Resource<TaskResponse> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val request = UpdateTaskStatusRequest(status = status)
            val response = RetrofitInstance.api.updateTaskStatus("Bearer $token", taskId, request)
             if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to update task status: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun deleteTask(taskId: String): Resource<SuccessApiResponseMessage> {
         return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val response = RetrofitInstance.api.deleteTask("Bearer $token", taskId)
             if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to delete task: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun getTaskComments(taskId: String): Resource<List<CommentResponse>> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val response = RetrofitInstance.api.getTaskComments("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to fetch comments: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    suspend fun addComment(taskId: String, content: String): Resource<CommentResponse> {
        return try {
            val token = getToken() ?: return Resource.Error("Authentication required")
            val user = dataStoreManager.user.first() ?: return Resource.Error("User not found")

            // Assuming userId in DataStore is String but convertible to Int for CommentRequest
            // If userId is a UUID string, this will fail.
            // If so, CommentRequest needs to be updated to String.
            // For now, trying conversion.
            val userIdInt = user.userId.toIntOrNull() ?: 0

            val request = CommentRequest(
                taskId = taskId.toInt(),
                content = content,
                userId = userIdInt
            )
            val response = RetrofitInstance.api.addComment("Bearer $token", request)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Empty response")
            } else {
                Resource.Error("Failed to add comment: ${response.message()}")
            }
        } catch (e: Exception) {
             Resource.Error("Error: ${e.message}")
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
