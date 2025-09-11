package com.example.todoapp.error

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import com.example.todoapp.R
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * 统一错误处理类
 * 负责将各种异常转换为用户友好的错误信息
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * 错误类型枚举
     */
    enum class ErrorType {
        NETWORK_ERROR,
        DATABASE_ERROR,
        VALIDATION_ERROR,
        PERMISSION_ERROR,
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
    fun handleException(exception: Throwable, context: Context): ErrorInfo {
        // 记录错误日志
        ErrorLogger.logError(context, "Exception occurred", exception)
        return when (exception) {
            is UnknownHostException -> {
                logError("网络连接失败", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "网络连接失败: ${exception.message}",
                    userMessage = context.getString(R.string.error_network_connection),
                    shouldRetry = true
                )
            }
            is IOException -> {
                logError("网络IO错误", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "网络IO错误: ${exception.message}",
                    userMessage = context.getString(R.string.error_network_io),
                    shouldRetry = true
                )
            }
            is TimeoutException -> {
                logError("请求超时", exception)
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = "请求超时: ${exception.message}",
                    userMessage = context.getString(R.string.error_timeout),
                    shouldRetry = true
                )
            }
            is SecurityException -> {
                logError("权限错误", exception)
                ErrorInfo(
                    type = ErrorType.PERMISSION_ERROR,
                    message = "权限错误: ${exception.message}",
                    userMessage = context.getString(R.string.error_permission),
                    shouldRetry = false
                )
            }
            is IllegalArgumentException -> {
                logError("参数验证错误", exception)
                ErrorInfo(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "参数验证错误: ${exception.message}",
                    userMessage = context.getString(R.string.error_validation),
                    shouldRetry = false
                )
            }
            is IllegalStateException -> {
                logError("状态错误", exception)
                ErrorInfo(
                    type = ErrorType.VALIDATION_ERROR,
                    message = "状态错误: ${exception.message}",
                    userMessage = context.getString(R.string.error_state),
                    shouldRetry = false
                )
            }
            else -> {
                logError("未知错误", exception)
                ErrorInfo(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = "未知错误: ${exception.message}",
                    userMessage = context.getString(R.string.error_unknown),
                    shouldRetry = false
                )
            }
        }
    }
    
    /**
     * 处理数据库相关错误
     */
    fun handleDatabaseError(exception: Throwable, context: Context): ErrorInfo {
        // 记录数据库错误日志
        ErrorLogger.logError(context, "Database error occurred", exception)
        logError("数据库错误", exception)
        return ErrorInfo(
            type = ErrorType.DATABASE_ERROR,
            message = "数据库错误: ${exception.message}",
            userMessage = context.getString(R.string.error_database),
            shouldRetry = true
        )
    }
    
    /**
     * 记录错误日志
     */
    private fun logError(message: String, exception: Throwable) {
        Log.e(TAG, message, exception)
        // 这里可以集成第三方崩溃报告工具，如Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().recordException(exception)
    }
    
    /**
     * 记录非异常错误
     */
    fun logError(message: String) {
        Log.e(TAG, message)
    }
    
    /**
     * 记录警告
     */
    fun logWarning(message: String) {
        Log.w(TAG, message)
    }
    
    /**
     * 记录信息
     */
    fun logInfo(message: String) {
        Log.i(TAG, message)
    }
    
    /**
     * 记录错误到文件（带Context）
     */
    fun logError(context: Context, message: String, exception: Throwable? = null) {
        ErrorLogger.logError(context, message, exception)
    }
    
    /**
     * 记录警告到文件（带Context）
     */
    fun logWarning(context: Context, message: String, exception: Throwable? = null) {
        ErrorLogger.logWarning(context, message, exception)
    }
    
    /**
     * 记录信息到文件（带Context）
     */
    fun logInfo(context: Context, message: String) {
        ErrorLogger.logInfo(context, message)
    }
}
