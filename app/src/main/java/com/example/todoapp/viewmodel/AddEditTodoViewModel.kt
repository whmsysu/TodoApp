package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Todo
import com.example.todoapp.notification.TodoNotificationManager
import com.example.todoapp.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    application: Application,
    private val repository: TodoRepository,
    private val notificationManager: TodoNotificationManager
) : AndroidViewModel(application) {

    private val _todo = MutableLiveData<Todo?>()
    val todo: LiveData<Todo?> = _todo

    private val _isEditMode = MutableLiveData<Boolean>()
    val isEditMode: LiveData<Boolean> = _isEditMode

    init {
        _isEditMode.value = false
    }

    fun loadTodo(todoId: Int) {
        viewModelScope.launch {
            when (val result = repository.getTodoById(todoId)) {
                is com.example.todoapp.result.Result.Success -> {
                    _todo.value = result.data
                    _isEditMode.value = result.data != null
                }
                is com.example.todoapp.result.Result.Error -> {
                    // 处理错误，可以显示错误消息
                    _todo.value = null
                    _isEditMode.value = false
                }
                is com.example.todoapp.result.Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }

    fun saveTodo(title: String, priority: Priority, dueDate: Date?, dueTime: String?, isDaily: Boolean, dailyTime: String?, dailyEndDate: Date?) {
        val currentTodo = _todo.value
        if (currentTodo != null) {
            // Edit existing todo
            val updatedTodo = currentTodo.copy(
                title = title,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                isDaily = isDaily,
                dailyTime = dailyTime,
                dailyEndDate = dailyEndDate
            )
            updateTodo(updatedTodo)
        } else {
            // Create new todo
            val newTodo = Todo(
                title = title,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                isDaily = isDaily,
                dailyTime = dailyTime,
                dailyEndDate = dailyEndDate
            )
            insertTodo(newTodo)
        }
    }

    private fun insertTodo(todo: Todo) {
        viewModelScope.launch {
            when (val result = repository.insertTodo(todo)) {
                is com.example.todoapp.result.Result.Success -> {
                    val savedTodo = todo.copy(id = result.data.toInt())
                    
                    // Schedule notification if it's a daily todo
                    if (savedTodo.isDaily) {
                        notificationManager.scheduleDailyNotification(savedTodo)
                    }
                }
                is com.example.todoapp.result.Result.Error -> {
                    // 处理插入错误
                }
                is com.example.todoapp.result.Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }

    private fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            when (repository.updateTodo(todo)) {
                is com.example.todoapp.result.Result.Success -> {
                    // Cancel existing notification and schedule new one if needed
                    notificationManager.cancelDailyNotification(todo.id)
                    if (todo.isDaily) {
                        notificationManager.scheduleDailyNotification(todo)
                    }
                }
                is com.example.todoapp.result.Result.Error -> {
                    // 处理更新错误
                }
                is com.example.todoapp.result.Result.Loading -> {
                    // 处理加载状态
                }
            }
        }
    }
}
