package com.example.todoapp.core.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.core.database.data.TodoDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling paged Todo data
 */
@Singleton
class TodoPagingRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    
    /**
     * Get paged todos with default configuration
     */
    fun getPagedTodos(): Flow<PagingData<Todo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                todoDao.getAllTodosPaged()
            }
        ).flow
    }
    
    /**
     * Get paged todos with custom configuration
     */
    fun getPagedTodos(
        pageSize: Int = 20,
        enablePlaceholders: Boolean = false,
        prefetchDistance: Int = 5
    ): Flow<PagingData<Todo>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = enablePlaceholders,
                prefetchDistance = prefetchDistance
            ),
            pagingSourceFactory = {
                todoDao.getAllTodosPaged()
            }
        ).flow
    }
}
