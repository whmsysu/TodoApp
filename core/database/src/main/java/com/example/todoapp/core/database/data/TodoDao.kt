package com.example.todoapp.core.database.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TodoDao {
    // 注意：这些查询已被移除，因为我们现在使用动态的完成状态判断
    // 所有过滤逻辑都在ViewModel中通过isTodoCompleted()函数处理

    // Sorted by due date/time and priority
    // Items without dueTime (NULL or empty string) are sorted after items with dueTime
    // Use '24:00' to ensure items without time are sorted last
    @Query("SELECT * FROM todos ORDER BY " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, " +
            "dueDate ASC, " +
            "CASE WHEN dueTime IS NULL OR dueTime = '' THEN '24:00' ELSE dueTime END ASC, " +
            "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC")
    fun getAllTodosSortedByDueDate(): LiveData<List<Todo>>
    
    @Query("SELECT * FROM todos ORDER BY " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, " +
            "dueDate ASC, " +
            "CASE WHEN dueTime IS NULL OR dueTime = '' THEN '24:00' ELSE dueTime END ASC, " +
            "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC")
    fun getAllTodosSortedByDueDateFlow(): Flow<List<Todo>>
    
    // Paging 3 support
    @Query("SELECT * FROM todos ORDER BY " +
            "CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, " +
            "dueDate ASC, " +
            "CASE WHEN dueTime IS NULL OR dueTime = '' THEN '24:00' ELSE dueTime END ASC, " +
            "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 1 END DESC")
    fun getAllTodosPaged(): androidx.paging.PagingSource<Int, Todo>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Int): Todo?

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Update
    suspend fun updateTodoStatus(todo: Todo)
}