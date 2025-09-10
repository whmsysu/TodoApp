package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
    val allTodos: LiveData<List<Todo>>
    val pendingTodos: LiveData<List<Todo>>
    val completedTodos: LiveData<List<Todo>>
    val dailyTodos: LiveData<List<Todo>>

    private val _currentFilter = MutableLiveData<TodoFilter>()
    val currentFilter: LiveData<TodoFilter> = _currentFilter

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        notificationManager = TodoNotificationManager(application)
        allTodos = repository.getAllTodos()
        pendingTodos = repository.getPendingTodos()
        completedTodos = repository.getCompletedTodos()
        dailyTodos = repository.getDailyTodos()
        _currentFilter.value = TodoFilter.ALL
    }

    fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            repository.insertTodo(todo)
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo)
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
            repository.updateTodoStatus(id, isCompleted)
        }
    }

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    fun getFilteredTodos(): LiveData<List<Todo>> {
        return when (_currentFilter.value) {
            TodoFilter.PENDING -> pendingTodos
            TodoFilter.COMPLETED -> completedTodos
            TodoFilter.DAILY -> dailyTodos
            else -> allTodos
        }
    }
}

enum class TodoFilter {
    ALL, PENDING, COMPLETED, DAILY
}
