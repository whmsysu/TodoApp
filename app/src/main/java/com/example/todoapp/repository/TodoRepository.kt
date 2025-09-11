package com.example.todoapp.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDao
import com.example.todoapp.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodosSortedByDueDate(): LiveData<List<Todo>> = todoDao.getAllTodosSortedByDueDate()

    suspend fun getTodoById(id: Int): Result<Todo?> = withContext(Dispatchers.IO) {
        try {
            val todo = todoDao.getTodoById(id)
            Result.Success(todo)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun insertTodo(todo: Todo): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = todoDao.insertTodo(todo)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateTodo(todo: Todo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            todoDao.updateTodo(todo)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteTodo(todo: Todo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            todoDao.deleteTodo(todo)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updateTodoStatus(todo: Todo): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            todoDao.updateTodoStatus(todo)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

}
