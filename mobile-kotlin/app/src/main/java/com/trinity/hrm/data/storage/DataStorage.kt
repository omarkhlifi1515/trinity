package com.trinity.hrm.data.storage

import android.content.Context
import com.trinity.hrm.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized data storage
 * Uses Supabase for real-time sync (replaces JSONBin.io)
 * Falls back to InMemoryStorage if Supabase not configured
 */
object DataStorage {
    private var storage: InMemoryStorage? = null
    private var useSupabase = false
    
    fun initialize(context: Context) {
        if (storage == null) {
            storage = InMemoryStorage(context)
        }
        
        // Check if Supabase is configured
        useSupabase = com.trinity.hrm.data.remote.SupabaseClient.isInitialized()
    }
    
    private fun requireStorage(): InMemoryStorage {
        return storage ?: throw IllegalStateException("DataStorage not initialized. Call initialize(context) first.")
    }
    
    private suspend fun <T> useSupabaseOrLocal(supabaseCall: suspend () -> T, localCall: suspend () -> T): T {
        return if (useSupabase) {
            try {
                supabaseCall()
            } catch (e: Exception) {
                println("Supabase call failed, falling back to local: ${e.message}")
                localCall()
            }
        } else {
            localCall()
        }
    }
    
    // Employees
    suspend fun getEmployees(): List<Employee> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getEmployees() },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getEmployees() } }
        )
    }
    
    suspend fun addEmployee(employee: Employee): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.addEmployee(employee) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().addEmployee(employee) } }
        )
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getTasks() },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getTasks() } }
        )
    }
    
    suspend fun addTask(task: Task): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.addTask(task) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().addTask(task) } }
        )
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getLeaves() },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getLeaves() } }
        )
    }
    
    suspend fun addLeave(leave: Leave): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.addLeave(leave) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().addLeave(leave) } }
        )
    }
    
    // Messages
    suspend fun getMessages(userId: String? = null): List<Message> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getMessages(userId) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getMessages() } }
        )
    }
    
    suspend fun addMessage(message: Message): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.addMessage(message) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().addMessage(message) } }
        )
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getDepartments() },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getDepartments() } }
        )
    }
    
    suspend fun addDepartment(department: Department): Boolean {
        return withContext(Dispatchers.IO) {
            requireStorage().addDepartment(department)
        }
    }
    
    // Attendance
    suspend fun getAttendance(userId: String? = null): List<Attendance> {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.getAttendance(userId) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().getAttendance() } }
        )
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.markAttendance(attendance) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().markAttendance(attendance) } }
        )
    }
    
    // Leave approval
    suspend fun approveLeave(leaveId: String, approverId: String): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.approveLeave(leaveId, approverId) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().approveLeave(leaveId, approverId) } }
        )
    }
    
    suspend fun rejectLeave(leaveId: String, approverId: String): Boolean {
        return useSupabaseOrLocal(
            supabaseCall = { com.trinity.hrm.data.storage.SupabaseStorage.rejectLeave(leaveId, approverId) },
            localCall = { withContext(Dispatchers.IO) { requireStorage().rejectLeave(leaveId, approverId) } }
        )
    }
    
    // Clear all data (for logout)
    fun clearAll() {
        storage?.clearAll()
    }
}

