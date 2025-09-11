package com.example.todoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Todo::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todos ADD COLUMN isDaily INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE todos ADD COLUMN dailyTime TEXT")
                database.execSQL("ALTER TABLE todos ADD COLUMN lastCompletedDate INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todos ADD COLUMN dailyEndDate INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todos ADD COLUMN dueTime TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 删除description字段
                database.execSQL("CREATE TABLE todos_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "priority TEXT NOT NULL, " +
                        "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                        "createdAt INTEGER NOT NULL, " +
                        "dueDate INTEGER, " +
                        "dueTime TEXT, " +
                        "isDaily INTEGER NOT NULL DEFAULT 0, " +
                        "dailyTime TEXT, " +
                        "dailyEndDate INTEGER, " +
                        "lastCompletedDate INTEGER)")
                
                database.execSQL("INSERT INTO todos_new (id, title, priority, isCompleted, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, lastCompletedDate) " +
                        "SELECT id, title, priority, isCompleted, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, lastCompletedDate FROM todos")
                
                database.execSQL("DROP TABLE todos")
                database.execSQL("ALTER TABLE todos_new RENAME TO todos")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加completedAt字段
                database.execSQL("ALTER TABLE todos ADD COLUMN completedAt INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 删除isCompleted字段
                database.execSQL("CREATE TABLE todos_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "priority TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "dueDate INTEGER, " +
                        "dueTime TEXT, " +
                        "isDaily INTEGER NOT NULL DEFAULT 0, " +
                        "dailyTime TEXT, " +
                        "dailyEndDate INTEGER, " +
                        "lastCompletedDate INTEGER, " +
                        "completedAt INTEGER)")
                
                database.execSQL("INSERT INTO todos_new (id, title, priority, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, lastCompletedDate, completedAt) " +
                        "SELECT id, title, priority, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, lastCompletedDate, completedAt FROM todos")
                
                database.execSQL("DROP TABLE todos")
                database.execSQL("ALTER TABLE todos_new RENAME TO todos")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 删除lastCompletedDate字段
                database.execSQL("CREATE TABLE todos_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "priority TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, " +
                        "dueDate INTEGER, " +
                        "dueTime TEXT, " +
                        "isDaily INTEGER NOT NULL DEFAULT 0, " +
                        "dailyTime TEXT, " +
                        "dailyEndDate INTEGER, " +
                        "completedAt INTEGER)")
                
                database.execSQL("INSERT INTO todos_new (id, title, priority, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, completedAt) " +
                        "SELECT id, title, priority, createdAt, dueDate, dueTime, isDaily, dailyTime, dailyEndDate, completedAt FROM todos")
                
                database.execSQL("DROP TABLE todos")
                database.execSQL("ALTER TABLE todos_new RENAME TO todos")
            }
        }
        
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加索引以优化查询性能
                database.execSQL("CREATE INDEX index_todos_dueDate ON todos (dueDate)")
                database.execSQL("CREATE INDEX index_todos_isDaily ON todos (isDaily)")
                database.execSQL("CREATE INDEX index_todos_completedAt ON todos (completedAt)")
                database.execSQL("CREATE INDEX index_todos_priority ON todos (priority)")
                database.execSQL("CREATE INDEX index_todos_dailyEndDate ON todos (dailyEndDate)")
            }
        }

        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
