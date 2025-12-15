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
 * In-memory storage with SharedPreferences backup
 * This provides session persistence and proper state management
 * Cloud sync is handled by Firebase Repositories
 */
class InMemoryStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trinity_hrm_data", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        // Storage initialized
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
             val jsonStr = prefs.getString("employees", "[]") ?: "[]"
             try {
                 employeesCache = json.decodeFromString<List<Employee>>(jsonStr).toMutableList()
             } catch (e: Exception) {
                 employeesCache = mutableListOf()
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
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        if (tasksCache == null) {
            val jsonStr = prefs.getString("tasks", "[]") ?: "[]"
            try {
                tasksCache = json.decodeFromString<List<Task>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                tasksCache = mutableListOf()
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
    
    private suspend fun saveTasks() {
        val jsonStr = json.encodeToString(tasksCache!!)
        prefs.edit().putString("tasks", jsonStr).apply()
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        if (leavesCache == null) {
            val jsonStr = prefs.getString("leaves", "[]") ?: "[]"
            try {
                leavesCache = json.decodeFromString<List<Leave>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                leavesCache = mutableListOf()
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
    
    suspend fun approveLeave(leaveId: String, approverId: String): Boolean {
        return try {
            if (leavesCache == null) getLeaves()
            val leave = leavesCache!!.find { it.id == leaveId }
            if (leave != null) {
                // val updated = leave.copy(status = LeaveStatus.APPROVED, approvedBy = approverId)
                // return updateLeave(leaveId, updated)
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun rejectLeave(leaveId: String, approverId: String): Boolean {
         return true 
    }
    
    private suspend fun saveLeaves() {
        val jsonStr = json.encodeToString(leavesCache!!)
        prefs.edit().putString("leaves", jsonStr).apply()
    }
    
    // Messages
    suspend fun getMessages(): List<Message> {
        if (messagesCache == null) {
            val jsonStr = prefs.getString("messages", "[]") ?: "[]"
            try {
                messagesCache = json.decodeFromString<List<Message>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                messagesCache = mutableListOf()
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
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        if (departmentsCache == null) {
            val jsonStr = prefs.getString("departments", "[]") ?: "[]"
            try {
                departmentsCache = json.decodeFromString<List<Department>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                departmentsCache = mutableListOf()
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
    }
    
    // Attendance
    suspend fun getAttendance(): List<Attendance> {
        if (attendanceCache == null) {
            val jsonStr = prefs.getString("attendance", "[]") ?: "[]"
            try {
                attendanceCache = json.decodeFromString<List<Attendance>>(jsonStr).toMutableList()
            } catch (e: Exception) {
                attendanceCache = mutableListOf()
            }
        }
        return attendanceCache!!.toList()
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return try {
            if (attendanceCache == null) getAttendance()
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
