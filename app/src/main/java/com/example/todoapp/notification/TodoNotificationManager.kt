package com.example.todoapp.notification

import android.content.Context
import androidx.work.*
import com.example.todoapp.data.Todo
import java.util.concurrent.TimeUnit

class TodoNotificationManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailyNotification(todo: Todo) {
        if (!todo.isDaily || todo.dailyTime == null) return

        val timeParts = todo.dailyTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val inputData = Data.Builder()
            .putInt(TodoNotificationWorker.NOTIFICATION_ID, todo.id)
            .putString(TodoNotificationWorker.TODO_TITLE, todo.title)
            .putString(TodoNotificationWorker.TODO_DESCRIPTION, todo.description)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<TodoNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("daily_todo_${todo.id}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_todo_${todo.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    fun cancelDailyNotification(todoId: Int) {
        workManager.cancelUniqueWork("daily_todo_$todoId")
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        
        // Set the target time for today
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val targetTime = calendar.timeInMillis
        
        // If the target time has passed today, schedule for tomorrow
        return if (targetTime <= now) {
            targetTime + TimeUnit.DAYS.toMillis(1) - now
        } else {
            targetTime - now
        }
    }
}
