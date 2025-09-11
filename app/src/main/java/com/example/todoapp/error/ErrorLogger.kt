package com.example.todoapp.error

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 错误日志记录器
 * 负责记录错误日志到文件和系统日志
 */
object ErrorLogger {
    
    private const val TAG = "ErrorLogger"
    private const val LOG_FILE_NAME = "error_log.txt"
    private const val MAX_LOG_SIZE = 1024 * 1024 // 1MB
    private const val MAX_LOG_FILES = 5
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 记录错误日志
     */
    fun logError(context: Context, message: String, exception: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logMessage = buildLogMessage(timestamp, "ERROR", message, exception)
        
        // 记录到系统日志
        if (exception != null) {
            Log.e(TAG, message, exception)
        } else {
            Log.e(TAG, message)
        }
        
        // 记录到文件
        writeToFile(context, logMessage)
    }
    
    /**
     * 记录警告日志
     */
    fun logWarning(context: Context, message: String, exception: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logMessage = buildLogMessage(timestamp, "WARNING", message, exception)
        
        // 记录到系统日志
        if (exception != null) {
            Log.w(TAG, message, exception)
        } else {
            Log.w(TAG, message)
        }
        
        // 记录到文件
        writeToFile(context, logMessage)
    }
    
    /**
     * 记录信息日志
     */
    fun logInfo(context: Context, message: String) {
        val timestamp = dateFormat.format(Date())
        val logMessage = buildLogMessage(timestamp, "INFO", message, null)
        
        // 记录到系统日志
        Log.i(TAG, message)
        
        // 记录到文件
        writeToFile(context, logMessage)
    }
    
    /**
     * 构建日志消息
     */
    private fun buildLogMessage(timestamp: String, level: String, message: String, exception: Throwable?): String {
        val sb = StringBuilder()
        sb.append("[$timestamp] [$level] $message")
        
        if (exception != null) {
            sb.append("\nException: ${exception.javaClass.simpleName}")
            sb.append("\nMessage: ${exception.message}")
            sb.append("\nStack Trace:")
            exception.stackTrace.forEach { element ->
                sb.append("\n  at $element")
            }
        }
        
        sb.append("\n" + "=".repeat(80) + "\n")
        return sb.toString()
    }
    
    /**
     * 写入日志文件
     */
    private fun writeToFile(context: Context, logMessage: String) {
        try {
            val logFile = getLogFile(context)
            
            // 检查文件大小，如果超过限制则轮转
            if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
                rotateLogFiles(context)
            }
            
            FileWriter(logFile, true).use { writer ->
                writer.write(logMessage)
                writer.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    /**
     * 获取日志文件
     */
    private fun getLogFile(context: Context): File {
        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        return File(logDir, LOG_FILE_NAME)
    }
    
    /**
     * 轮转日志文件
     */
    private fun rotateLogFiles(context: Context) {
        val logDir = File(context.filesDir, "logs")
        val logFile = File(logDir, LOG_FILE_NAME)
        
        // 删除最老的日志文件
        val oldestLogFile = File(logDir, "$LOG_FILE_NAME.${MAX_LOG_FILES - 1}")
        if (oldestLogFile.exists()) {
            oldestLogFile.delete()
        }
        
        // 重命名现有日志文件
        for (i in MAX_LOG_FILES - 1 downTo 1) {
            val currentFile = if (i == 1) logFile else File(logDir, "$LOG_FILE_NAME.$i")
            val nextFile = File(logDir, "$LOG_FILE_NAME.${i + 1}")
            
            if (currentFile.exists()) {
                currentFile.renameTo(nextFile)
            }
        }
    }
    
    /**
     * 获取所有日志文件
     */
    fun getLogFiles(context: Context): List<File> {
        val logDir = File(context.filesDir, "logs")
        if (!logDir.exists()) {
            return emptyList()
        }
        
        return logDir.listFiles { file ->
            file.name.startsWith(LOG_FILE_NAME)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * 清除所有日志文件
     */
    fun clearLogs(context: Context) {
        val logFiles = getLogFiles(context)
        logFiles.forEach { it.delete() }
        Log.i(TAG, "All log files cleared")
    }
    
    /**
     * 获取日志文件总大小
     */
    fun getLogFilesSize(context: Context): Long {
        val logFiles = getLogFiles(context)
        return logFiles.sumOf { it.length() }
    }
}
