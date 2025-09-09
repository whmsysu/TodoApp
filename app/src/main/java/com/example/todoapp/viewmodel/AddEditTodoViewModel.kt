package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.repository.TodoRepository
import kotlinx.coroutines.launch
import java.util.Date

class AddEditTodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    private val _todo = MutableLiveData<Todo?>()
    val todo: LiveData<Todo?> = _todo

    private val _isEditMode = MutableLiveData<Boolean>()
    val isEditMode: LiveData<Boolean> = _isEditMode

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        _isEditMode.value = false
    }

    fun loadTodo(todoId: Int) {
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            _todo.value = todo
            _isEditMode.value = todo != null
        }
    }

    fun saveTodo(title: String, description: String, priority: Priority, dueDate: Date?) {
        val currentTodo = _todo.value
        if (currentTodo != null) {
            // Edit existing todo
            val updatedTodo = currentTodo.copy(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            )
            updateTodo(updatedTodo)
        } else {
            // Create new todo
            val newTodo = Todo(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            )
            insertTodo(newTodo)
        }
    }

    private fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            repository.insertTodo(todo)
        }
    }

    private fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }
}
