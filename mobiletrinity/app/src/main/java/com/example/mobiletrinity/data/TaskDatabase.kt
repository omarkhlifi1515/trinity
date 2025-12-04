package com.example.mobiletrinity.data

import androidx.room.*
import androidx.room.Database

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val createdAt: String
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasks(): List<Task>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getTasksByStatus(status: String): List<Task>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<Task>)
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var instance: TaskDatabase? = null
        
        fun getInstance(context: android.content.Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "trinity_database"
                ).build().also { instance = it }
            }
        }
    }
}
