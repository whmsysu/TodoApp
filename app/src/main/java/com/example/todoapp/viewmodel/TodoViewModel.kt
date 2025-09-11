package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.error.ErrorHandler
import com.example.todoapp.notification.TodoNotificationManager
import com.example.todoapp.repository.TodoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.Calendar
import java.util.Date

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: TodoRepository
    private var notificationManager: TodoNotificationManager
    private val _currentFilter = MutableLiveData<TodoFilter>()
    val currentFilter: LiveData<TodoFilter> = _currentFilter
    
    // Single LiveData that combines database data with filter
    private val _filteredTodos = MediatorLiveData<List<Todo>>()
    val filteredTodos: LiveData<List<Todo>> = _filteredTodos
    
    // Error handling
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _errorInfo = MutableLiveData<ErrorHandler.ErrorInfo>()
    val errorInfo: LiveData<ErrorHandler.ErrorInfo> = _errorInfo
    
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        val errorInfo = ErrorHandler.handleException(exception, getApplication())
        _errorInfo.postValue(errorInfo)
        _errorMessage.postValue(errorInfo.userMessage)
    }

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        notificationManager = TodoNotificationManager(application)
        _currentFilter.value = TodoFilter.PENDING
        
        // Set up filtered todos LiveData
        setupFilteredTodos()
    }

    fun insertTodo(todo: Todo) {
        viewModelScope.launch(exceptionHandler) {
            try {
                repository.insertTodo(todo)
            } catch (e: Exception) {
                val errorInfo = ErrorHandler.handleDatabaseError(e, getApplication())
                _errorInfo.postValue(errorInfo)
                _errorMessage.postValue(getApplication<Application>().getString(R.string.error_save_todo))
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch(exceptionHandler) {
            try {
                repository.deleteTodo(todo)
                // Cancel notification if it's a daily todo
                if (todo.isDaily) {
                    notificationManager.cancelDailyNotification(todo.id)
                }
            } catch (e: Exception) {
                val errorInfo = ErrorHandler.handleDatabaseError(e, getApplication())
                _errorInfo.postValue(errorInfo)
                _errorMessage.postValue(getApplication<Application>().getString(R.string.error_delete_todo))
            }
        }
    }

    fun updateTodoStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            try {
                // Get the todo first, then update it using @Update to ensure LiveData updates
                val todo = repository.getTodoById(id)
                todo?.let {
                    val updatedTodo = it.copy(
                        completedAt = if (isCompleted) Date() else null
                    )
                    repository.updateTodoStatus(updatedTodo)
                }
            } catch (e: Exception) {
                val errorInfo = ErrorHandler.handleDatabaseError(e, getApplication())
                _errorInfo.postValue(errorInfo)
                _errorMessage.postValue(getApplication<Application>().getString(R.string.error_update_todo))
            }
        }
    }

    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
        _errorInfo.value = null
    }
    
    fun retryLastOperation() {
        // 这里可以实现重试逻辑
        // 例如：重新加载数据、重新执行上次失败的操作等
        ErrorHandler.logInfo("用户请求重试操作")
    }

    private fun isTodoCompleted(todo: Todo): Boolean {
        // 如果completedAt为空，说明未完成
        val completedAt = todo.completedAt ?: return false
        
        // 如果是每日TODO，需要检查是否是今天完成的
        if (todo.isDaily) {
            val today = Calendar.getInstance()
            val completedDate = Calendar.getInstance()
            completedDate.time = completedAt
            
            return today.get(Calendar.YEAR) == completedDate.get(Calendar.YEAR) &&
                   today.get(Calendar.DAY_OF_YEAR) == completedDate.get(Calendar.DAY_OF_YEAR)
        }
        
        // 普通TODO，只要有completedAt就算完成
        return true
    }
    
    private fun isDailyTodoExpired(todo: Todo): Boolean {
        if (!todo.isDaily) return false
        
        val dailyEndDate = todo.dailyEndDate ?: return false // 没有结束日期，不过期
        val today = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.time = dailyEndDate
        
        // 设置时间为00:00:00进行比较
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        endDate.set(Calendar.HOUR_OF_DAY, 0)
        endDate.set(Calendar.MINUTE, 0)
        endDate.set(Calendar.SECOND, 0)
        endDate.set(Calendar.MILLISECOND, 0)
        
        return today.after(endDate)
    }

    private var currentTodos: List<Todo> = emptyList()

    private fun setupFilteredTodos() {

        fun updateFilteredTodos() {
            val currentFilter = _currentFilter.value ?: TodoFilter.PENDING
            
            val filteredList = when (currentFilter) {
                TodoFilter.PENDING -> currentTodos.filter { !isTodoCompleted(it) && !isDailyTodoExpired(it) }
                TodoFilter.COMPLETED -> currentTodos.filter { isTodoCompleted(it) && !isDailyTodoExpired(it) }
                TodoFilter.DAILY -> currentTodos.filter { it.isDaily && !isDailyTodoExpired(it) }
            }
            _filteredTodos.value = filteredList.sortedWith(compareBy<Todo> { todo ->
                // 1. 首先按日期排序 (null日期排在最后)
                todo.dueDate?.time ?: Long.MAX_VALUE
            }.thenBy { todo ->
                // 2. 相同日期内，按时间排序 (没有时间的排在最后)
                when {
                    todo.dueTime.isNullOrBlank() -> "24:00" // 没有时间的排在最后
                    else -> todo.dueTime
                }
            }.thenByDescending { todo ->
                // 3. 最后按优先级排序 (高优先级在前)
                when (todo.priority) {
                    com.example.todoapp.data.Priority.HIGH -> 3
                    com.example.todoapp.data.Priority.MEDIUM -> 2
                    com.example.todoapp.data.Priority.LOW -> 1
                }
            })
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
    PENDING, COMPLETED, DAILY
}
