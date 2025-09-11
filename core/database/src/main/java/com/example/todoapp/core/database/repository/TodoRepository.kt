package com.example.todoapp.core.database.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.core.database.data.TodoDao
import com.example.todoapp.core.common.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {

    fun getAllTodos(): Flow<Result<List<Todo>>> = flow {
        try {
            emit(Result.Loading)
            todoDao.getAllTodosSortedByDueDateFlow().collect { todos ->
                emit(Result.Success(todos))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

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
