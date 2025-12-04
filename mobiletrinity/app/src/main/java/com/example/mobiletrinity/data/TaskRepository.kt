package com.example.mobiletrinity.data

import com.example.mobiletrinity.api.ApiService
import com.example.mobiletrinity.api.TaskRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TaskRepository(
    private val apiService: ApiService,
    private val taskDao: TaskDao
) {
    
    suspend fun syncTasks(): Result<Unit> = try {
        val remoteTasks = apiService.getTasks()
        val localTasks = remoteTasks.map { 
            Task(
                id = it.id,
                title = it.title,
                description = it.description,
                priority = it.priority,
                status = it.status,
                dueDate = it.dueDate,
                createdAt = it.createdAt
            )
        }
        taskDao.deleteAllTasks()
        taskDao.insertAllTasks(localTasks)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    fun getTasksFlow(): Flow<List<Task>> = flow {
        val tasks = taskDao.getAllTasks()
        emit(tasks)
    }
    
    suspend fun getTasksByStatus(status: String): Result<List<Task>> = try {
        Result.success(taskDao.getTasksByStatus(status))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun createTask(title: String, description: String, priority: String, dueDate: String): Result<Task> = try {
        val request = TaskRequest(title, description, priority, dueDate)
        val response = apiService.createTask(request)
        val task = Task(
            id = response.id,
            title = response.title,
            description = response.description,
            priority = response.priority,
            status = response.status,
            dueDate = response.dueDate,
            createdAt = response.createdAt
        )
        taskDao.insertTask(task)
        Result.success(task)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun updateTaskStatus(taskId: Int, newStatus: String): Result<Task> = try {
        val request = com.example.mobiletrinity.api.StatusUpdateRequest(taskId, newStatus)
        val response = apiService.updateTaskStatus(taskId, request)
        val task = Task(
            id = response.id,
            title = response.title,
            description = response.description,
            priority = response.priority,
            status = response.status,
            dueDate = response.dueDate,
            createdAt = response.createdAt
        )
        taskDao.updateTask(task)
        Result.success(task)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend fun deleteTask(taskId: Int): Result<Unit> = try {
        apiService.deleteTask(taskId)
        taskDao.deleteTask(taskDao.getTaskById(taskId) ?: return Result.success(Unit))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
