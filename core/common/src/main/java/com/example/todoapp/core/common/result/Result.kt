package com.example.todoapp.core.common.result

/**
 * 表示操作结果的密封类
 * 用于统一处理成功和失败的情况
 */
sealed class Result<out T> {
    /**
     * 成功结果
     */
    data class Success<T>(val data: T) : Result<T>()
    
    /**
     * 错误结果
     */
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    /**
     * 加载中状态
     */
    object Loading : Result<Nothing>()
}

/**
 * 扩展函数：检查结果是否为成功
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * 扩展函数：检查结果是否为错误
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * 扩展函数：检查结果是否为加载中
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * 扩展函数：获取成功数据，如果失败则返回null
 */
fun <T> Result<T>.getDataOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * 扩展函数：获取错误异常，如果不是错误则返回null
 */
fun <T> Result<T>.getErrorOrNull(): Throwable? = when (this) {
    is Result.Error -> exception
    else -> null
}

/**
 * 扩展函数：获取成功数据，如果失败则返回默认值
 */
fun <T> Result<T>.getDataOrDefault(defaultValue: T): T = when (this) {
    is Result.Success -> data
    else -> defaultValue
}

/**
 * 扩展函数：映射成功数据
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> Result.Error(exception)
    is Result.Loading -> Result.Loading
}

/**
 * 扩展函数：映射错误
 */
inline fun <T> Result<T>.mapError(transform: (Throwable) -> Throwable): Result<T> = when (this) {
    is Result.Success -> Result.Success(data)
    is Result.Error -> Result.Error(transform(exception))
    is Result.Loading -> Result.Loading
}

/**
 * 扩展函数：处理结果
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * 扩展函数：处理错误
 */
inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * 扩展函数：处理加载状态
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        action()
    }
    return this
}

/**
 * 扩展函数：获取数据或抛出异常
 */
fun <T> Result<T>.getDataOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
    is Result.Loading -> throw IllegalStateException("Result is still loading")
}
