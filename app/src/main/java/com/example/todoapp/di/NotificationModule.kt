package com.example.todoapp.di

import android.content.Context
import com.example.todoapp.notification.TodoNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt模块，提供通知相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideTodoNotificationManager(@ApplicationContext context: Context): TodoNotificationManager {
        return TodoNotificationManager(context)
    }
}
