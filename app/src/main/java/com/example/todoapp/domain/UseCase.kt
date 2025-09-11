package com.example.todoapp.domain

import com.example.todoapp.core.common.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Base UseCase interface for all use cases
 */
interface UseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): Result<R>
}

/**
 * Base UseCase interface for use cases that return Flow
 */
interface FlowUseCase<in P, out R> {
    operator fun invoke(parameters: P): Flow<Result<R>>
}

/**
 * Base UseCase interface for use cases without parameters
 */
interface NoParamsUseCase<out R> {
    suspend operator fun invoke(): Result<R>
}

/**
 * Base UseCase interface for use cases without parameters that return Flow
 */
interface NoParamsFlowUseCase<out R> {
    operator fun invoke(): Flow<Result<R>>
}
