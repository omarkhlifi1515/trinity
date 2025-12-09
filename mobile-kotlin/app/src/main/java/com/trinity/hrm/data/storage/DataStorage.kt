package com.trinity.hrm.data.storage

import com.trinity.hrm.data.model.*
import com.trinity.hrm.data.remote.JsonBinClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Centralized data storage using JSONBin.io
 * Stores all HRM data (employees, tasks, leaves, messages, departments, attendance)
 */
object DataStorage {
    private const val EMPLOYEES_BIN_KEY = "employees_bin_id"
    private const val TASKS_BIN_KEY = "tasks_bin_id"
    private const val LEAVES_BIN_KEY = "leaves_bin_id"
    private const val MESSAGES_BIN_KEY = "messages_bin_id"
    private const val DEPARTMENTS_BIN_KEY = "departments_bin_id"
    private const val ATTENDANCE_BIN_KEY = "attendance_bin_id"
    
    // Employees
    suspend fun getEmployees(): List<Employee> {
        return withContext(Dispatchers.IO) {
            try {
                val binId = getBinId(EMPLOYEES_BIN_KEY)
                if (binId == null) return@withContext emptyList()
                
                val users = JsonBinClient.readUsers()
                // For now, convert users to employees
                // In future, store employees separately
                users.map { user ->
                    Employee(
                        id = user.id,
                        name = user.email.split("@")[0],
                        email = user.email,
                        department = user.department,
                        createdAt = user.createdAt ?: ""
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun addEmployee(employee: Employee): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val employees = getEmployees().toMutableList()
                employees.add(employee)
                return@withContext saveEmployees(employees)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private suspend fun saveEmployees(employees: List<Employee>): Boolean {
        // For now, we'll store employees in a separate structure
        // This is a placeholder - implement full JSONBin storage later
        return true
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val binId = getBinId(TASKS_BIN_KEY)
                if (binId == null) return@withContext emptyList()
                // Implement reading from JSONBin
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun addTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val tasks = getTasks().toMutableList()
                tasks.add(task)
                // Implement saving to JSONBin
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        return withContext(Dispatchers.IO) {
            try {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun addLeave(leave: Leave): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val leaves = getLeaves().toMutableList()
                leaves.add(leave)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Messages
    suspend fun getMessages(): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun addMessage(message: Message): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val messages = getMessages().toMutableList()
                messages.add(message)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        return withContext(Dispatchers.IO) {
            try {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun addDepartment(department: Department): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val departments = getDepartments().toMutableList()
                departments.add(department)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Attendance
    suspend fun getAttendance(): List<Attendance> {
        return withContext(Dispatchers.IO) {
            try {
                emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val attendanceList = getAttendance().toMutableList()
                attendanceList.add(attendance)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Helper to get/set bin IDs (store in SharedPreferences or JSONBin metadata)
    private suspend fun getBinId(key: String): String? {
        // Implement getting bin ID from storage
        return null
    }
}

