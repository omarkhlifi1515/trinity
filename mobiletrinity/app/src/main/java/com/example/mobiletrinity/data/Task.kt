package com.example.mobiletrinity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val createdAt: String = ""
)
