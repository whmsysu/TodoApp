package com.example.todoapp

import android.app.Application
import android.util.Log
import com.example.todoapp.core.common.error.ErrorHandler
import com.example.todoapp.network.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用全局Application类
 * 负责全局配置和异常处理
 */
@HiltAndroidApp
class TodoApplication : Application() {
    
    companion object {
        private const val TAG = "TodoApplication"
    }
    
    lateinit var networkMonitor: NetworkMonitor
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // 设置全局未捕获异常处理器
        setupGlobalExceptionHandler()
        
        // 初始化其他全局配置
        initializeApp()
    }
    
    /**
     * 设置全局异常处理器
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // 记录异常信息
            // ErrorHandler.logError(this@TodoApplication, "未捕获的异常", exception)
            // 使用Android Log直接记录
            android.util.Log.e("TodoApplication", "未捕获的异常", exception)
            
            // 可以在这里添加崩溃报告逻辑
            // 例如：Firebase Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(exception)
            
            // 调用默认处理器
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
    
    /**
     * 初始化应用配置
     */
    private fun initializeApp() {
        Log.i(TAG, "TodoApplication initialized")
        
        // 初始化网络监控
        networkMonitor = NetworkMonitor(this)
        networkMonitor.startMonitoring()
        
        // 这里可以添加其他全局初始化逻辑
        // 例如：初始化第三方SDK、设置全局配置等
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 停止网络监控
        if (::networkMonitor.isInitialized) {
            networkMonitor.stopMonitoring()
        }
    }
}
