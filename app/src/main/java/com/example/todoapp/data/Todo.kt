package com.example.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val priority: Priority,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val dueDate: Date? = null,
    val dueTime: String? = null, // Format: "HH:mm"
    val isDaily: Boolean = false,
    val dailyTime: String? = null, // Format: "HH:mm"
    val dailyEndDate: Date? = null, // End date for daily repetition
    val lastCompletedDate: Date? = null,
    val completedAt: Date? = null // Time when the todo was completed
)

enum class Priority {
    LOW, MEDIUM, HIGH
}
