package com.example.todoapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.notification.TodoNotificationManager
import com.example.todoapp.repository.TodoRepository
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    private val notificationManager: TodoNotificationManager
    private val _currentFilter = MutableLiveData<TodoFilter>()
    val currentFilter: LiveData<TodoFilter> = _currentFilter
    
    // Single LiveData that combines database data with filter
    private val _filteredTodos = MediatorLiveData<List<Todo>>()
    val filteredTodos: LiveData<List<Todo>> = _filteredTodos

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        notificationManager = TodoNotificationManager(application)
        _currentFilter.value = TodoFilter.ALL
        
        // Set up filtered todos LiveData
        setupFilteredTodos()
    }

    fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            repository.insertTodo(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
            // Cancel notification if it's a daily todo
            if (todo.isDaily) {
                notificationManager.cancelDailyNotification(todo.id)
            }
        }
    }

    fun updateTodoStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            // Get the todo first, then update it using @Update to ensure LiveData updates
            val todo = repository.getTodoById(id)
            todo?.let {
                val updatedTodo = it.copy(isCompleted = isCompleted)
                repository.updateTodoStatus(updatedTodo)
            }
        }
    }

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    private var currentTodos: List<Todo> = emptyList()

    private fun setupFilteredTodos() {

        fun updateFilteredTodos() {
            val currentFilter = _currentFilter.value ?: TodoFilter.ALL
            
            val filteredList = when (currentFilter) {
                TodoFilter.PENDING -> currentTodos.filter { !it.isCompleted }
                TodoFilter.COMPLETED -> currentTodos.filter { it.isCompleted }
                TodoFilter.DAILY -> currentTodos.filter { it.isDaily }
                else -> currentTodos
            }
            _filteredTodos.value = filteredList
        }
        
        // Update when all todos change
        _filteredTodos.addSource(repository.getAllTodosSortedByDueDate()) { todos ->
            currentTodos = todos
            updateFilteredTodos()
        }
        
        // Update when filter changes
        _filteredTodos.addSource(_currentFilter) {
            updateFilteredTodos()
        }
    }
}

enum class TodoFilter {
    ALL, PENDING, COMPLETED, DAILY
}
