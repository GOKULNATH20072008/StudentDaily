package com.example.studentdaily

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studentdaily.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val classId = intent.getLongExtra("classId", -1L)
        val type = intent.getStringExtra("type") ?: return

        if (classId == -1L) return

        val database = AppDatabase.getInstance(context)
        val notificationHelper = NotificationHelper(context)

        CoroutineScope(Dispatchers.IO).launch {
            val classEntity = database.classDao().getClassById(classId) ?: return@launch
            val settings = database.settingsDao().getSettingsSync() ?: return@launch

            val title: String
            val content: String

            when (type) {
                "REMINDER" -> {
                    if (!settings.classRemindersEnabled) return@launch
                    title = classEntity.subjectName
                    content = "${classEntity.subjectName} starts in ${settings.reminderMinutesBefore} minutes."
                }
                "START" -> {
                    if (!settings.notifyClassStart) return@launch
                    title = "${classEntity.subjectName} starting"
                    content = "${classEntity.subjectName} class is starting now."
                }
                "END" -> {
                    if (!settings.notifyClassEnd) return@launch
                    title = "${classEntity.subjectName} ended"
                    content = "${classEntity.subjectName} class has ended."
                }
                else -> return@launch
            }

            val typeOffset = when (type) {
                "REMINDER" -> 1
                "START" -> 2
                "END" -> 3
                else -> 0
            }

            notificationHelper.showNotification(
                NotificationHelper.CLASS_REMINDERS_CHANNEL_ID,
                title,
                content,
                classId.toInt() * 10 + typeOffset,
                soundEnabled = settings.soundEnabled,
                vibrationEnabled = settings.vibrationEnabled
            )

            // Reschedule for next week
            val reminderManager = ClassReminderManager(context)
            reminderManager.scheduleClassAlarms(classEntity, settings)
        }
    }
}
