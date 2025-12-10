package com.trinity.hrm.data.storage

import com.trinity.hrm.data.model.*
import com.trinity.hrm.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Supabase Data Storage
 * Replaces JSONBin.io with Supabase for real-time sync
 */
object SupabaseStorage {
    
    // Employees
    suspend fun getEmployees(): List<Employee> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val response = supabase.from("employees")
                    .select()
                    .decodeList<EmployeeSupabase>()
                
                response.map { it.toEmployee() }
            } catch (e: Exception) {
                println("Error fetching employees: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun addEmployee(employee: Employee): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                supabase.from("employees")
                    .insert(employee.toSupabase())
                true
            } catch (e: Exception) {
                println("Error adding employee: ${e.message}")
                false
            }
        }
    }
    
    // Tasks
    suspend fun getTasks(): List<Task> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val response = supabase.from("tasks")
                    .select()
                    .decodeList<TaskSupabase>()
                
                response.map { it.toTask() }
            } catch (e: Exception) {
                println("Error fetching tasks: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun addTask(task: Task): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                supabase.from("tasks")
                    .insert(task.toSupabase())
                true
            } catch (e: Exception) {
                println("Error adding task: ${e.message}")
                false
            }
        }
    }
    
    // Leaves
    suspend fun getLeaves(): List<Leave> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val response = supabase.from("leaves")
                    .select()
                    .decodeList<LeaveSupabase>()
                
                response.map { it.toLeave() }
            } catch (e: Exception) {
                println("Error fetching leaves: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun addLeave(leave: Leave): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                supabase.from("leaves")
                    .insert(leave.toSupabase())
                true
            } catch (e: Exception) {
                println("Error adding leave: ${e.message}")
                false
            }
        }
    }
    
    suspend fun approveLeave(leaveId: String, approverId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                // Fetch all leaves, find the one to update
                val leaves = supabase.from("leaves")
                    .select()
                    .decodeList<LeaveSupabase>()
                
                val leave = leaves.find { it.id == leaveId }
                if (leave != null) {
                    val updated = leave.copy(
                        status = "approved",
                        approved_by = approverId
                    )
                    // Update using upsert (insert with same ID will replace)
                    supabase.from("leaves").upsert(updated)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                println("Error approving leave: ${e.message}")
                false
            }
        }
    }
    
    suspend fun rejectLeave(leaveId: String, approverId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                // Fetch all leaves, find the one to update
                val leaves = supabase.from("leaves")
                    .select()
                    .decodeList<LeaveSupabase>()
                
                val leave = leaves.find { it.id == leaveId }
                if (leave != null) {
                    val updated = leave.copy(
                        status = "rejected",
                        approved_by = approverId
                    )
                    // Use upsert to update (will replace if exists, insert if not)
                    supabase.from("leaves").upsert(updated)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                println("Error rejecting leave: ${e.message}")
                false
            }
        }
    }
    
    // Messages
    suspend fun getMessages(userId: String? = null): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val query = supabase.from("messages")
                    .select()
                
                if (userId != null) {
                    // RLS will handle filtering, but we can also filter client-side
                }
                
                val response = query.decodeList<MessageSupabase>()
                response.map { it.toMessage() }
            } catch (e: Exception) {
                println("Error fetching messages: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun addMessage(message: Message): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                supabase.from("messages")
                    .insert(message.toSupabase())
                true
            } catch (e: Exception) {
                println("Error adding message: ${e.message}")
                false
            }
        }
    }
    
    // Departments
    suspend fun getDepartments(): List<Department> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val response = supabase.from("departments")
                    .select()
                    .decodeList<DepartmentSupabase>()
                
                response.map { it.toDepartment() }
            } catch (e: Exception) {
                println("Error fetching departments: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Attendance
    suspend fun getAttendance(userId: String? = null): List<Attendance> {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext emptyList()
                val response = supabase.from("attendance")
                    .select()
                    .decodeList<AttendanceSupabase>()
                
                response.map { it.toAttendance() }
            } catch (e: Exception) {
                println("Error fetching attendance: ${e.message}")
                emptyList()
            }
        }
    }
    
    suspend fun markAttendance(attendance: Attendance): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val supabase = SupabaseClient.getClient() ?: return@withContext false
                // Delete existing attendance for same date and employee first
                // Fetch all, find matching, delete, then insert
                val allAttendance = supabase.from("attendance")
                    .select()
                    .decodeList<AttendanceSupabase>()
                
                // Use upsert to replace existing attendance for same date/employee
                // The UNIQUE constraint on (employee_id, date) will handle duplicates
                supabase.from("attendance")
                    .upsert(attendance.toSupabase())
                true
            } catch (e: Exception) {
                println("Error marking attendance: ${e.message}")
                false
            }
        }
    }
    
    // Real-time subscriptions (simplified - can be enhanced later)
    fun subscribeToMessages(callback: (Message) -> Unit) {
        // TODO: Implement real-time subscriptions when needed
        // For now, use polling or manual refresh
        println("Real-time subscriptions not yet implemented")
    }
}

// Data class mappings for Supabase (snake_case)
@Serializable
data class EmployeeSupabase(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val department: String? = null,
    val position: String? = null,
    val hire_date: String? = null,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toEmployee(): Employee {
        return Employee(
            id = id,
            name = name,
            email = email,
            phone = phone,
            department = department,
            position = position,
            hireDate = hire_date,
            createdAt = created_at
        )
    }
}

fun Employee.toSupabase(): EmployeeSupabase {
    return EmployeeSupabase(
        id = id,
        name = name,
        email = email,
        phone = phone,
        department = department,
        position = position,
        hire_date = hireDate,
        created_at = createdAt,
        updated_at = null
    )
}

@Serializable
data class TaskSupabase(
    val id: String,
    val title: String,
    val description: String? = null,
    val assigned_to: String? = null,
    val assigned_by: String,
    val status: String,
    val priority: String,
    val due_date: String? = null,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toTask(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            assignedTo = assigned_to ?: "",
            assignedBy = assigned_by,
            status = TaskStatus.valueOf(status.uppercase()),
            priority = TaskPriority.valueOf(priority.uppercase()),
            dueDate = due_date,
            createdAt = created_at
        )
    }
}

fun Task.toSupabase(): TaskSupabase {
    return TaskSupabase(
        id = id,
        title = title,
        description = description,
        assigned_to = assignedTo,
        assigned_by = assignedBy,
        status = status.name.lowercase(),
        priority = priority.name.lowercase(),
        due_date = dueDate,
        created_at = createdAt,
        updated_at = null
    )
}

@Serializable
data class LeaveSupabase(
    val id: String,
    val employee_id: String,
    val type: String,
    val start_date: String,
    val end_date: String,
    val reason: String? = null,
    val status: String,
    val approved_by: String? = null,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toLeave(): Leave {
        return Leave(
            id = id,
            employeeId = employee_id,
            type = LeaveType.valueOf(type.uppercase()),
            startDate = start_date,
            endDate = end_date,
            reason = reason,
            status = LeaveStatus.valueOf(status.uppercase()),
            approvedBy = approved_by,
            createdAt = created_at
        )
    }
}

fun Leave.toSupabase(): LeaveSupabase {
    return LeaveSupabase(
        id = id,
        employee_id = employeeId,
        type = type.name.lowercase(),
        start_date = startDate,
        end_date = endDate,
        reason = reason,
        status = status.name,
        approved_by = approvedBy,
        created_at = createdAt,
        updated_at = null
    )
}

@Serializable
data class MessageSupabase(
    val id: String,
    val from_user: String,
    val to_user: String? = null,
    val subject: String,
    val content: String,
    val read: Boolean,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toMessage(): Message {
        return Message(
            id = id,
            from = from_user,
            to = to_user ?: "",
            subject = subject,
            content = content,
            read = read,
            createdAt = created_at
        )
    }
}

fun Message.toSupabase(): MessageSupabase {
    return MessageSupabase(
        id = id,
        from_user = from,
        to_user = to,
        subject = subject,
        content = content,
        read = read,
        created_at = createdAt,
        updated_at = null
    )
}

@Serializable
data class DepartmentSupabase(
    val id: String,
    val name: String,
    val description: String? = null,
    val head_id: String? = null,
    val employee_count: Int,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toDepartment(): Department {
        return Department(
            id = id,
            name = name,
            description = description,
            headId = head_id,
            employeeCount = employee_count,
            createdAt = created_at
        )
    }
}

@Serializable
data class AttendanceSupabase(
    val id: String,
    val employee_id: String,
    val date: String,
    val check_in: String? = null,
    val check_out: String? = null,
    val status: String,
    val notes: String? = null,
    val created_at: String,
    val updated_at: String? = null
) {
    fun toAttendance(): Attendance {
        return Attendance(
            id = id,
            employeeId = employee_id,
            date = date,
            checkIn = check_in,
            checkOut = check_out,
            status = AttendanceStatus.valueOf(status.uppercase()),
            notes = notes
        )
    }
}

fun Attendance.toSupabase(): AttendanceSupabase {
    return AttendanceSupabase(
        id = id,
        employee_id = employeeId,
        date = date,
        check_in = checkIn,
        check_out = checkOut,
        status = status.name.lowercase(),
        notes = notes,
        created_at = java.time.Instant.now().toString(),
        updated_at = null
    )
}

