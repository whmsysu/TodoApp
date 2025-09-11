package com.example.todoapp.domain.usecase

import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.domain.NoParamsFlowUseCase
import com.example.todoapp.core.database.repository.TodoRepository
import com.example.todoapp.core.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * UseCase for getting all todos
 */
class GetTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) : NoParamsFlowUseCase<List<Todo>> {
    
    override fun invoke(): Flow<Result<List<Todo>>> {
        return repository.getAllTodos()
    }
}
