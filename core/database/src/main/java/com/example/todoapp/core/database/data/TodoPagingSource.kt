package com.example.todoapp.core.database.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.todoapp.core.database.data.TodoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Paging 3 data source for Todo items
 */
class TodoPagingSource(
    private val todoDao: TodoDao
) : PagingSource<Int, Todo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Todo> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // For now, we'll load all todos and paginate them
            // In a real implementation, you'd modify the DAO to support pagination
            // val todos = withContext(Dispatchers.IO) {
            //     todoDao.getAllTodosSortedByDueDateFlow()
            // }

            // Since we're using Flow, we need to collect it
            // This is a simplified implementation
            val allTodos = emptyList<Todo>() // Placeholder
            
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, allTodos.size)
            val pageData = allTodos.subList(startIndex, endIndex)

            LoadResult.Page(
                data = pageData,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (endIndex >= allTodos.size) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Todo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
