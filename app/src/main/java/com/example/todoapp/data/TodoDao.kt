package com.example.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Date

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun getAllTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getPendingTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): LiveData<List<Todo>>

    // Sorted by due date/time and priority
    @Query("SELECT * FROM todos ORDER BY " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, " +
            "dueDate ASC, " +
            "CASE WHEN dueTime IS NULL THEN 1 ELSE 0 END, " +
            "dueTime ASC, " +
            "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC")
    fun getAllTodosSortedByDueDate(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, " +
            "dueDate ASC, " +
            "CASE WHEN dueTime IS NULL THEN 1 ELSE 0 END, " +
            "dueTime ASC, " +
            "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC")
    fun getPendingTodosSortedByDueDate(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Int): Todo?

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTodoStatus(id: Int, isCompleted: Boolean)

    @Query("SELECT * FROM todos WHERE isDaily = 1 ORDER BY dailyTime ASC")
    fun getDailyTodos(): LiveData<List<Todo>>

    @Query("UPDATE todos SET lastCompletedDate = :date WHERE id = :id")
    suspend fun updateLastCompletedDate(id: Int, date: Date)
}
