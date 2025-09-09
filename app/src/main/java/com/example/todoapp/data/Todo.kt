package com.example.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val priority: Priority,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val dueDate: Date? = null
)

enum class Priority {
    LOW, MEDIUM, HIGH
}
