package com.example.todoapp.feature.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.core.database.repository.TodoPagingRepository
import com.example.todoapp.core.common.error.ErrorHandler
import com.example.todoapp.core.common.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoPagingViewModel @Inject constructor(
    application: Application,
    private val todoPagingRepository: TodoPagingRepository
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Paging data flow
    val pagedTodos: Flow<PagingData<Todo>> = todoPagingRepository
        .getPagedTodos()
        .cachedIn(viewModelScope)

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Paging data is automatically loaded through the flow
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                val errorInfo = ErrorHandler.handleException(e, getApplication())
                _errorMessage.value = errorInfo.userMessage
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun refresh() {
        loadTodos()
    }
}
