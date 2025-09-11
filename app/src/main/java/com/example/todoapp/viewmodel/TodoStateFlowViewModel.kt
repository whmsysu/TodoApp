package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.domain.usecase.DeleteTodoUseCase
import com.example.todoapp.domain.usecase.GetTodosUseCase
import com.example.todoapp.domain.usecase.InsertTodoUseCase
import com.example.todoapp.domain.usecase.UpdateTodoUseCase
import com.example.todoapp.core.common.error.ErrorHandler
import com.example.todoapp.notification.TodoNotificationManager
import com.example.todoapp.core.common.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoStateFlowViewModel @Inject constructor(
    application: Application,
    private val getTodosUseCase: GetTodosUseCase,
    private val insertTodoUseCase: InsertTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val notificationManager: TodoNotificationManager
) : AndroidViewModel(application) {

    private val _currentFilter = MutableStateFlow(TodoFilter.PENDING)
    val currentFilter: StateFlow<TodoFilter> = _currentFilter.asStateFlow()

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    private val _filteredTodos = MutableStateFlow<List<Todo>>(emptyList())
    val filteredTodos: StateFlow<List<Todo>> = _filteredTodos.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Combine todos and filter to create filtered todos
        viewModelScope.launch {
            combine(_todos, _currentFilter) { todos, filter ->
                filterTodos(todos, filter)
            }.collect { filtered ->
                _filteredTodos.value = filtered
            }
        }

        // Load initial data
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            getTodosUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _todos.value = result.data
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                        _errorMessage.value = errorInfo.userMessage
                    }
                }
            }
        }
    }

    fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = insertTodoUseCase(todo)) {
                is Result.Success -> {
                    // Success - data will be updated through Flow
                    if (todo.isDaily && !todo.dailyTime.isNullOrEmpty()) {
                        notificationManager.scheduleDailyNotification(todo)
                    }
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = updateTodoUseCase(todo)) {
                is Result.Success -> {
                    // Success - data will be updated through Flow
                    if (todo.isDaily && !todo.dailyTime.isNullOrEmpty()) {
                        notificationManager.scheduleDailyNotification(todo)
                    } else {
                        notificationManager.cancelDailyNotification(todo.id)
                    }
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = deleteTodoUseCase(todo)) {
                is Result.Success -> {
                    // Success - data will be updated through Flow
                    notificationManager.cancelDailyNotification(todo.id)
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    private fun filterTodos(todos: List<Todo>, filter: TodoFilter): List<Todo> {
        return when (filter) {
            TodoFilter.PENDING -> todos.filter { !isTodoCompleted(it) }
            TodoFilter.COMPLETED -> todos.filter { isTodoCompleted(it) }
            TodoFilter.DAILY -> todos.filter { it.isDaily && !isDailyTodoExpired(it) }
        }
    }

    private fun isTodoCompleted(todo: Todo): Boolean {
        return todo.completedAt != null
    }

    private fun isDailyTodoExpired(todo: Todo): Boolean {
        if (!todo.isDaily || todo.dailyEndDate == null) return false
        
        val today = java.util.Calendar.getInstance()
        val endDate = java.util.Calendar.getInstance().apply {
            time = todo.dailyEndDate
        }
        
        return today.after(endDate)
    }
}
