package com.example.todoapp.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(): LiveData<List<Todo>> = todoDao.getAllTodos()

    fun getPendingTodos(): LiveData<List<Todo>> = todoDao.getPendingTodos()

    fun getCompletedTodos(): LiveData<List<Todo>> = todoDao.getCompletedTodos()

    suspend fun getTodoById(id: Int): Todo? = withContext(Dispatchers.IO) {
        todoDao.getTodoById(id)
    }

    suspend fun insertTodo(todo: Todo): Long = withContext(Dispatchers.IO) {
        todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.deleteTodo(todo)
    }

    suspend fun updateTodoStatus(id: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        todoDao.updateTodoStatus(id, isCompleted)
    }
}
