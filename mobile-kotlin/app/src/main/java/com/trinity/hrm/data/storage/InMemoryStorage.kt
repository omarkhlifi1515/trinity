package com.trinity.hrm.data.storage

import com.trinity.hrm.data.model.*
import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * In-memory storage with SharedPreferences backup and JSONBin.io sync
 * This provides session persistence and proper state management
 * Also syncs with web app and React Native app via JSONBin.io
 */
class InMemoryStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trinity_hrm_data", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        // Storage initialized - no cloud sync needed (using Supabase via DataStorage)
    }
    
    // In-memory caches
    private var employeesCache: MutableList<Employee>? = null
    private var tasksCache: MutableList<Task>? = null
    private var leavesCache: MutableList<Leave>? = null
    private var messagesCache: MutableList<Message>? = null
    private var departmentsCache: MutableList<Department>? = null
    private var attendanceCache: MutableList<Attendance>? = null
    
    // Employees
    suspend fun getEmployees(): List<Employee> {
        if (employeesCache == null) {
            // Try to sync from cloud first
            syncEmployeesFromCloud()
            
            // Fallback to local if cloud sync failed
            if (employeesCache == null || employeesCache!!.isEmpty()) {
                val jsonStr = prefs.getString("employees", "[]") ?: "[]"
                try {
                    employeesCache = json.decodeFromString<List<Employee>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    employeesCache = mutableListOf()
                }
            }
        }
        return employeesCache!!.toList()
    }
    
    suspend fun addEmployee(employee: Employee): Boolean {
        return try {
            if (employeesCache == null) getEmployees()
            employeesCache!!.add(employee)
            saveEmployees()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveEmployees() {
        val jsonStr = json.encodeToString(employeesCache!!)
        prefs.edit().putString("employees", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncEmployeesFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
        // This is kept for backward compatibility but does nothing
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        if (tasksCache == null) {
            syncTasksFromCloud()
            if (tasksCache == null || tasksCache!!.isEmpty()) {
                val jsonStr = prefs.getString("tasks", "[]") ?: "[]"
                try {
                    tasksCache = json.decodeFromString<List<Task>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    tasksCache = mutableListOf()
                }
            }
        }
        return tasksCache!!.toList()
    }
    
    suspend fun addTask(task: Task): Boolean {
        return try {
            if (tasksCache == null) getTasks()
            tasksCache!!.add(task)
            saveTasks()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateTask(id: String, updates: Task): Boolean {
        return try {
            if (tasksCache == null) getTasks()
            val index = tasksCache!!.indexOfFirst { it.id == id }
            if (index != -1) {
                tasksCache!![index] = updates
                saveTasks()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveTasks() {
        val jsonStr = json.encodeToString(tasksCache!!)
        prefs.edit().putString("tasks", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncTasksFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        if (leavesCache == null) {
            syncLeavesFromCloud()
            if (leavesCache == null || leavesCache!!.isEmpty()) {
                val jsonStr = prefs.getString("leaves", "[]") ?: "[]"
                try {
                    leavesCache = json.decodeFromString<List<Leave>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    leavesCache = mutableListOf()
                }
            }
        }
        return leavesCache!!.toList()
    }
    
    suspend fun addLeave(leave: Leave): Boolean {
        return try {
            if (leavesCache == null) getLeaves()
            leavesCache!!.add(leave)
            saveLeaves()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateLeave(id: String, updates: Leave): Boolean {
        return try {
            if (leavesCache == null) getLeaves()
            val index = leavesCache!!.indexOfFirst { it.id == id }
            if (index != -1) {
                leavesCache!![index] = updates
                saveLeaves()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun approveLeave(leaveId: String, approverId: String): Boolean {
        return try {
            if (leavesCache == null) getLeaves()
            val leave = leavesCache!!.find { it.id == leaveId }
            if (leave != null) {
                val updated = leave.copy(status = com.trinity.hrm.data.model.LeaveStatus.APPROVED, approvedBy = approverId)
                return updateLeave(leaveId, updated)
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun rejectLeave(leaveId: String, approverId: String): Boolean {
        return try {
            if (leavesCache == null) getLeaves()
            val leave = leavesCache!!.find { it.id == leaveId }
            if (leave != null) {
                val updated = leave.copy(status = com.trinity.hrm.data.model.LeaveStatus.REJECTED, approvedBy = approverId)
                return updateLeave(leaveId, updated)
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveLeaves() {
        val jsonStr = json.encodeToString(leavesCache!!)
        prefs.edit().putString("leaves", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncLeavesFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    // Messages
    suspend fun getMessages(): List<Message> {
        if (messagesCache == null) {
            syncMessagesFromCloud()
            if (messagesCache == null || messagesCache!!.isEmpty()) {
                val jsonStr = prefs.getString("messages", "[]") ?: "[]"
                try {
                    messagesCache = json.decodeFromString<List<Message>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    messagesCache = mutableListOf()
                }
            }
        }
        return messagesCache!!.toList()
    }
    
    suspend fun addMessage(message: Message): Boolean {
        return try {
            if (messagesCache == null) getMessages()
            messagesCache!!.add(message)
            saveMessages()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveMessages() {
        val jsonStr = json.encodeToString(messagesCache!!)
        prefs.edit().putString("messages", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncMessagesFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        if (departmentsCache == null) {
            syncDepartmentsFromCloud()
            if (departmentsCache == null || departmentsCache!!.isEmpty()) {
                val jsonStr = prefs.getString("departments", "[]") ?: "[]"
                try {
                    departmentsCache = json.decodeFromString<List<Department>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    departmentsCache = mutableListOf()
                }
            }
        }
        return departmentsCache!!.toList()
    }
    
    suspend fun addDepartment(department: Department): Boolean {
        return try {
            if (departmentsCache == null) getDepartments()
            departmentsCache!!.add(department)
            saveDepartments()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveDepartments() {
        val jsonStr = json.encodeToString(departmentsCache!!)
        prefs.edit().putString("departments", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncDepartmentsFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    // Attendance
    suspend fun getAttendance(): List<Attendance> {
        if (attendanceCache == null) {
            syncAttendanceFromCloud()
            if (attendanceCache == null || attendanceCache!!.isEmpty()) {
                val jsonStr = prefs.getString("attendance", "[]") ?: "[]"
                try {
                    attendanceCache = json.decodeFromString<List<Attendance>>(jsonStr).toMutableList()
                } catch (e: Exception) {
                    attendanceCache = mutableListOf()
                }
            }
        }
        return attendanceCache!!.toList()
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return try {
            if (attendanceCache == null) getAttendance()
            // Remove existing attendance for same date and employee
            attendanceCache!!.removeAll { it.date == attendance.date && it.employeeId == attendance.employeeId }
            attendanceCache!!.add(attendance)
            saveAttendance()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun saveAttendance() {
        val jsonStr = json.encodeToString(attendanceCache!!)
        prefs.edit().putString("attendance", jsonStr).apply()
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    private suspend fun syncAttendanceFromCloud() {
        // Cloud sync handled by DataStorage (Supabase)
    }
    
    // Clear all data (for logout)
    fun clearAll() {
        employeesCache = null
        tasksCache = null
        leavesCache = null
        messagesCache = null
        departmentsCache = null
        attendanceCache = null
        prefs.edit().clear().apply()
    }
}
