package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.data.TodoDao
import com.example.todoapp.data.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt模块，提供数据库相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo_database"
        )
        .addMigrations(
            TodoDatabase.MIGRATION_1_2,
            TodoDatabase.MIGRATION_2_3,
            TodoDatabase.MIGRATION_3_4,
            TodoDatabase.MIGRATION_4_5,
            TodoDatabase.MIGRATION_5_6,
            TodoDatabase.MIGRATION_6_7,
            TodoDatabase.MIGRATION_7_8,
            TodoDatabase.MIGRATION_8_9
        )
        .build()
    }

    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }
}
