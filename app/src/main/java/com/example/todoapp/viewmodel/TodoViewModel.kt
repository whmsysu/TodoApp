package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.core.database.repository.TodoRepository
import com.example.todoapp.core.common.error.ErrorHandler
import com.example.todoapp.core.common.result.Result
import com.example.todoapp.notification.TodoNotificationManager
import com.example.todoapp.viewmodel.TodoFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    application: Application,
    private val todoRepository: TodoRepository,
    private val notificationManager: TodoNotificationManager
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _errorInfo = MutableStateFlow<ErrorHandler.ErrorInfo?>(null)
    val errorInfo: StateFlow<ErrorHandler.ErrorInfo?> = _errorInfo.asStateFlow()
    
    private val _currentFilter = MutableStateFlow(TodoFilter.PENDING)
    val currentFilter: StateFlow<TodoFilter> = _currentFilter.asStateFlow()

    // Filtered todos flow (replacing paging for now to support filtering)
    private val _filteredTodos = MutableStateFlow<List<Todo>>(emptyList())
    val filteredTodos: StateFlow<List<Todo>> = _filteredTodos.asStateFlow()
    
    // Store all todos for local filtering
    private var allTodos: List<Todo> = emptyList()
    

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Load all todos and apply current filter
                todoRepository.getAllTodos().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            applyCurrentFilter(result.data)
                            _isLoading.value = false
                        }
                        is Result.Error -> {
                            val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                            _errorInfo.value = errorInfo
                            _errorMessage.value = errorInfo.userMessage
                            _isLoading.value = false
                        }
                        is Result.Loading -> {
                            // Keep loading state
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorInfo = ErrorHandler.handleException(e, getApplication())
                _errorMessage.value = errorInfo.userMessage
            }
        }
    }
    
    private fun applyCurrentFilter(todos: List<Todo>) {
        // Store all todos for local filtering
        allTodos = todos
        
        applyCurrentFilterToStoredData()
    }
    
    private fun applyCurrentFilterToStoredData() {
        android.util.Log.d("TodoViewModel", "Applying filter: ${_currentFilter.value}, Total todos: ${allTodos.size}")
        
        val filteredList = when (_currentFilter.value) {
            TodoFilter.PENDING -> {
                val result = allTodos.filter { !isTodoCompleted(it) && !isDailyTodoExpired(it) }
                android.util.Log.d("TodoViewModel", "PENDING filter: ${result.size} items")
                result
            }
            TodoFilter.COMPLETED -> {
                val result = allTodos.filter { isTodoCompleted(it) && !isDailyTodoExpired(it) }
                android.util.Log.d("TodoViewModel", "COMPLETED filter: ${result.size} items")
                // Log details of completed todos
                allTodos.forEach { todo ->
                    android.util.Log.d("TodoViewModel", "Todo ${todo.id}: completedAt=${todo.completedAt}, isDaily=${todo.isDaily}, dailyEndDate=${todo.dailyEndDate}")
                }
                result
            }
            TodoFilter.DAILY -> {
                val result = allTodos.filter { it.isDaily && !isDailyTodoExpired(it) }
                android.util.Log.d("TodoViewModel", "DAILY filter: ${result.size} items")
                result
            }
        }
        
        // Sort the filtered list
        val sortedList = filteredList.sortedWith(compareBy<Todo> { todo ->
            // 1. 首先按日期排序 (null日期排在最后)
            todo.dueDate?.time ?: Long.MAX_VALUE
        }.thenBy { todo ->
            // 2. 相同日期内，按时间排序 (没有时间的排在最后)
            when {
                todo.dueTime.isNullOrBlank() -> "24:00" // 没有时间的排在最后
                else -> todo.dueTime
            }
        })
        
        _filteredTodos.value = sortedList
    }
    
    private fun isTodoCompleted(todo: Todo): Boolean {
        return todo.completedAt != null
    }
    
    private fun isDailyTodoExpired(todo: Todo): Boolean {
        if (!todo.isDaily) return false
        val endDate = todo.dailyEndDate ?: return false
        return endDate.before(java.util.Date())
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        _errorInfo.value = null
    }

    fun refresh() {
        loadTodos()
    }
    
    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
        // Apply filter to stored data locally
        applyCurrentFilterToStoredData()
    }
    
    fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = todoRepository.insertTodo(todo)) {
                is Result.Success -> {
                    // 成功插入，重新加载数据
                    loadTodos()
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorInfo.value = errorInfo
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }
    
    fun updateTodoStatus(todoId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            when (val result = todoRepository.getTodoById(todoId)) {
                is Result.Success -> {
                    result.data?.let { todo ->
                        val updatedTodo = if (isCompleted) {
                            todo.copy(completedAt = java.util.Date())
                        } else {
                            todo.copy(completedAt = null)
                        }
                        updateTodo(updatedTodo)
                    }
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorInfo.value = errorInfo
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }
    
    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = todoRepository.updateTodo(todo)) {
                is Result.Success -> {
                    // Cancel existing notification and schedule new one if needed
                    notificationManager.cancelDailyNotification(todo.id)
                    if (todo.isDaily && todo.completedAt == null) {
                        notificationManager.scheduleDailyNotification(todo)
                    }
                    // 成功更新，重新加载数据
                    loadTodos()
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorInfo.value = errorInfo
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }
    
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = todoRepository.deleteTodo(todo)) {
                is Result.Success -> {
                    // Cancel notification if it's a daily todo
                    if (todo.isDaily) {
                        notificationManager.cancelDailyNotification(todo.id)
                    }
                    // 成功删除，重新加载数据
                    loadTodos()
                }
                is Result.Error -> {
                    val errorInfo = ErrorHandler.handleException(result.exception, getApplication())
                    _errorInfo.value = errorInfo
                    _errorMessage.value = errorInfo.userMessage
                }
                is Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }
    
    fun retryLastOperation() {
        // 这里可以实现重试逻辑
        android.util.Log.i("TodoViewModel", "用户请求重试操作")
    }
}
