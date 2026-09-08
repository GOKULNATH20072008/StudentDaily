package com.example.studentdaily

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.studentdaily.data.model.ClassEntity
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.NotificationSettingsEntity
import java.util.*

class ClassReminderManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleClassAlarms(classEntity: ClassEntity, settings: NotificationSettingsEntity) {
        if (settings.classRemindersEnabled) {
            scheduleAlarm(classEntity, "REMINDER", settings.reminderMinutesBefore)
        } else {
            cancelAlarm(classEntity, "REMINDER")
        }

        if (settings.notifyClassStart) {
            scheduleAlarm(classEntity, "START", 0)
        } else {
            cancelAlarm(classEntity, "START")
        }

        if (settings.notifyClassEnd) {
            scheduleAlarm(classEntity, "END", 0, isEnd = true)
        } else {
            cancelAlarm(classEntity, "END")
        }
    }

    fun cancelAllClassAlarms(classEntity: ClassEntity) {
        cancelAlarm(classEntity, "REMINDER")
        cancelAlarm(classEntity, "START")
        cancelAlarm(classEntity, "END")
    }

    private fun scheduleAlarm(classEntity: ClassEntity, type: String, minutesBefore: Int, isEnd: Boolean = false) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("classId", classEntity.id)
            putExtra("type", type)
        }

        val typeOffset = when (type) {
            "REMINDER" -> 1
            "START" -> 2
            "END" -> 3
            else -> 0
        }
        val requestCode = classEntity.id.toInt() * 10 + typeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = getNextOccurrence(classEntity, minutesBefore, isEnd)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(classEntity: ClassEntity, type: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val typeOffset = when (type) {
            "REMINDER" -> 1
            "START" -> 2
            "END" -> 3
            else -> 0
        }
        val requestCode = classEntity.id.toInt() * 10 + typeOffset
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getNextOccurrence(classEntity: ClassEntity, minutesBefore: Int, isEnd: Boolean): Calendar {
        val timeStr = if (isEnd) classEntity.endTime else classEntity.startTime
        val parts = timeStr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val dayOfWeekInt = when (classEntity.dayOfWeek) {
            DayOfWeek.MON -> Calendar.MONDAY
            DayOfWeek.TUE -> Calendar.TUESDAY
            DayOfWeek.WED -> Calendar.WEDNESDAY
            DayOfWeek.THU -> Calendar.THURSDAY
            DayOfWeek.FRI -> Calendar.FRIDAY
            DayOfWeek.SAT -> Calendar.SATURDAY
            DayOfWeek.SUN -> Calendar.SUNDAY
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeekInt)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -minutesBefore)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar
    }
}
