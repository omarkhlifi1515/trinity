package com.trinity.hrm.data.storage

import android.content.Context
import com.trinity.hrm.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized data storage
 * Uses local InMemoryStorage with SharedPreferences backup
 * Firebase is used for authentication only
 */
object DataStorage {
    private var storage: InMemoryStorage? = null
    
    fun initialize(context: Context) {
        if (storage == null) {
            storage = InMemoryStorage(context)
        }
    }
    
    private fun requireStorage(): InMemoryStorage {
        return storage ?: throw IllegalStateException("DataStorage not initialized. Call initialize(context) first.")
    }
    
    // Employees
    suspend fun getEmployees(): List<Employee> {
        return withContext(Dispatchers.IO) { requireStorage().getEmployees() }
    }
    
    suspend fun addEmployee(employee: Employee): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().addEmployee(employee) }
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        return withContext(Dispatchers.IO) { requireStorage().getTasks() }
    }
    
    suspend fun addTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().addTask(task) }
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        return withContext(Dispatchers.IO) { requireStorage().getLeaves() }
    }
    
    suspend fun addLeave(leave: Leave): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().addLeave(leave) }
    }
    
    // Messages
    suspend fun getMessages(userId: String? = null): List<Message> {
        return withContext(Dispatchers.IO) { requireStorage().getMessages() }
    }
    
    suspend fun addMessage(message: Message): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().addMessage(message) }
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        return withContext(Dispatchers.IO) { requireStorage().getDepartments() }
    }
    
    suspend fun addDepartment(department: Department): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().addDepartment(department) }
    }
    
    // Attendance
    suspend fun getAttendance(userId: String? = null): List<Attendance> {
        return withContext(Dispatchers.IO) { requireStorage().getAttendance() }
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().markAttendance(attendance) }
    }
    
    // Leave approval
    suspend fun approveLeave(leaveId: String, approverId: String): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().approveLeave(leaveId, approverId) }
    }
    
    suspend fun rejectLeave(leaveId: String, approverId: String): Boolean {
        return withContext(Dispatchers.IO) { requireStorage().rejectLeave(leaveId, approverId) }
    }
    
    // Clear all data (for logout)
    fun clearAll() {
        storage?.clearAll()
    }
}

