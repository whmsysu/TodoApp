package com.example.todoapp.core.common.error

import android.content.Context
import android.util.Log
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * 统一错误处理器
 * 处理各种异常并提供用户友好的错误信息
 */
object ErrorHandler {

    /**
     * 错误类型枚举
     */
    enum class ErrorType {
        NETWORK_ERROR,
        DATABASE_ERROR,
        VALIDATION_ERROR,
        UNKNOWN_ERROR
    }

    /**
     * 错误信息数据类
     */
    data class ErrorInfo(
        val type: ErrorType,
        val message: String,
        val userMessage: String,
        val shouldRetry: Boolean = false
    )

    /**
     * 处理异常并返回错误信息
     */
    fun handleException(exception: Throwable, @Suppress("UNUSED_PARAMETER") context: Context): ErrorInfo {
        // 记录错误日志
        logError("Exception occurred", exception)
        return when (exception) {
            is UnknownHostException -> {
                logError("网络连接失败", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "网络连接失败: ${exception.message}",
                    userMessage = "网络连接失败，请检查网络设置",
                    shouldRetry = true
                )
            }
            is IOException -> {
                logError("网络IO错误", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "网络IO错误: ${exception.message}",
                    userMessage = "网络连接异常，请稍后重试",
                    shouldRetry = true
                )
            }
            is TimeoutException -> {
                logError("请求超时", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "请求超时: ${exception.message}",
                    userMessage = "请求超时，请稍后重试",
                    shouldRetry = true
                )
            }
            is SecurityException -> {
                logError("权限错误", exception)
                ErrorInfo(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "权限错误: ${exception.message}",
                    userMessage = "权限不足，请检查应用权限设置",
                    shouldRetry = false
                )
            }
            is IllegalArgumentException -> {
                logError("参数错误", exception)
                ErrorInfo(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "参数错误: ${exception.message}",
                    userMessage = "输入参数有误，请检查后重试",
                    shouldRetry = false
                )
            }
            is IllegalStateException -> {
                logError("状态错误", exception)
                ErrorInfo(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "状态错误: ${exception.message}",
                    userMessage = "操作状态异常，请重新尝试",
                    shouldRetry = true
                )
            }
            is NullPointerException -> {
                logError("空指针异常", exception)
                ErrorInfo(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "空指针异常: ${exception.message}",
                    userMessage = "应用出现异常，请重启应用",
                    shouldRetry = false
                )
            }
            is RuntimeException -> {
                logError("运行时异常", exception)
                ErrorInfo(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "运行时异常: ${exception.message}",
                    userMessage = "应用出现异常，请稍后重试",
                    shouldRetry = true
                )
            }
            else -> {
                logError("未知异常", exception)
                ErrorInfo(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "未知异常: ${exception.message}",
                    userMessage = "发生未知错误，请稍后重试",
                    shouldRetry = true
                )
            }
        }
    }

    /**
     * 记录错误日志
     */
    private fun logError(message: String, exception: Throwable) {
        Log.e("ErrorHandler", message, exception)
    }

    /**
     * 记录错误日志（无异常）
     */
    private fun logError(message: String) {
        Log.e("ErrorHandler", message)
    }
}