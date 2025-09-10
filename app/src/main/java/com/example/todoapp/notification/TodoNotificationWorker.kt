package com.example.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.MainActivity
import com.example.todoapp.R

class TodoNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "daily_todo_channel"
        const val NOTIFICATION_ID = "notification_id"
        const val TODO_TITLE = "todo_title"
        const val TODO_DESCRIPTION = "todo_description"
    }

    override fun doWork(): Result {
        val notificationId = inputData.getInt(NOTIFICATION_ID, 0)
        val todoTitle = inputData.getString(TODO_TITLE) ?: "每日待办事项"
        val todoDescription = inputData.getString(TODO_DESCRIPTION) ?: ""

        createNotificationChannel()
        showNotification(notificationId, todoTitle, todoDescription)
        
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "每日待办事项提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日固定时间的待办事项提醒"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(notificationId: Int, title: String, description: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description.ifEmpty { "该完成你的每日待办事项了！" })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
