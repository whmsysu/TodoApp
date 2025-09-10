package com.example.todoapp.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodosSortedByDueDate(): LiveData<List<Todo>> = todoDao.getAllTodosSortedByDueDate()

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
    
    suspend fun updateTodoStatus(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.updateTodoStatus(todo)
    }

}
