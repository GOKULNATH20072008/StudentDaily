package com.example.studentdaily

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.studentdaily.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val database = AppDatabase.getInstance(context)
            val reminderManager = ClassReminderManager(context)

            CoroutineScope(Dispatchers.IO).launch {
                val allClasses = database.classDao().getAllClasses().first()
                val settings = database.settingsDao().getSettingsSync() ?: return@launch
                
                allClasses.forEach { classEntity ->
                    reminderManager.scheduleClassAlarms(classEntity, settings)
                }
            }
        }
    }
}
