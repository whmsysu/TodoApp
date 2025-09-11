package com.example.todoapp.domain.usecase

import com.example.todoapp.core.database.data.Todo
import com.example.todoapp.domain.UseCase
import com.example.todoapp.core.database.repository.TodoRepository
import com.example.todoapp.core.common.result.Result
import javax.inject.Inject

/**
 * UseCase for updating an existing todo
 */
class UpdateTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) : UseCase<Todo, Unit> {
    
    override suspend fun invoke(parameters: Todo): Result<Unit> {
        return repository.updateTodo(parameters)
    }
}
