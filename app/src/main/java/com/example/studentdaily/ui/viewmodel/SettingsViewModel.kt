package com.example.studentdaily.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentdaily.ClassReminderManager
import com.example.studentdaily.data.AppDatabase
import com.example.studentdaily.data.dao.ClassDao
import com.example.studentdaily.data.dao.SettingsDao
import com.example.studentdaily.data.model.NotificationSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val database: AppDatabase,
    private val settingsDao: SettingsDao,
    private val classDao: ClassDao,
    context: Context
) : ViewModel() {
    private val reminderManager = ClassReminderManager(context)

    val settings: Flow<NotificationSettingsEntity?> = settingsDao.getSettings()
        .onEach { 
            if (it == null) {
                viewModelScope.launch {
                    settingsDao.upsertSettings(NotificationSettingsEntity())
                }
            }
        }

    fun updateSettings(newSettings: NotificationSettingsEntity) {
        viewModelScope.launch {
            val oldSettings = settingsDao.getSettingsSync()
            settingsDao.upsertSettings(newSettings)
            
            // If class notification settings changed, reschedule all alarms
            if (oldSettings?.classRemindersEnabled != newSettings.classRemindersEnabled ||
                oldSettings?.reminderMinutesBefore != newSettings.reminderMinutesBefore ||
                oldSettings?.notifyClassStart != newSettings.notifyClassStart ||
                oldSettings?.notifyClassEnd != newSettings.notifyClassEnd) {
                
                val allClasses = classDao.getAllClasses().first()
                allClasses.forEach { 
                    reminderManager.scheduleClassAlarms(it, newSettings)
                }
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Cancel all alarms
                val allClasses = classDao.getAllClasses().first()
                allClasses.forEach { 
                    reminderManager.cancelAllClassAlarms(it)
                }

                // 2. Clear all database tables in IO thread
                withContext(Dispatchers.IO) {
                    database.clearAllTables()
                }

                // 3. Restore default settings
                settingsDao.upsertSettings(NotificationSettingsEntity())

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (_: Exception) {
                // Silently fail
            }
        }
    }
}
