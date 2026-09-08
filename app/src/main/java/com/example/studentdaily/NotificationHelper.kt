package com.example.studentdaily

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {
    companion object {
        const val CLASS_REMINDERS_CHANNEL_ID = "CLASS_REMINDERS"
        const val EXPENSE_ALERTS_CHANNEL_ID = "EXPENSE_ALERTS"
        const val DAILY_SUMMARY_CHANNEL_ID = "DAILY_SUMMARY"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val classRemindersChannel = NotificationChannel(
                CLASS_REMINDERS_CHANNEL_ID,
                "Class Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming classes"
            }

            val expenseAlertsChannel = NotificationChannel(
                EXPENSE_ALERTS_CHANNEL_ID,
                "Expense Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when expense limits are reached"
            }

            val dailySummaryChannel = NotificationChannel(
                DAILY_SUMMARY_CHANNEL_ID,
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning summary of your day"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(classRemindersChannel)
            manager.createNotificationChannel(expenseAlertsChannel)
            manager.createNotificationChannel(dailySummaryChannel)
        }
    }

    fun showNotification(
        channelId: String,
        title: String,
        content: String,
        notificationId: Int,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (!soundEnabled && !vibrationEnabled) {
            builder.setSilent(true)
        } else {
            if (!soundEnabled) {
                builder.setSound(null)
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            }
            if (!vibrationEnabled) {
                builder.setVibrate(longArrayOf(0))
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
            if (soundEnabled && vibrationEnabled) {
                builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
