package com.example.todoapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun getAllTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getPendingTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): LiveData<List<Todo>>

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
}
