package com.example.studentdaily.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.room.withTransaction
import com.example.studentdaily.ClassReminderManager
import com.example.studentdaily.data.AppDatabase
import com.example.studentdaily.data.model.ExportData
import com.example.studentdaily.data.model.NotificationSettingsEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupViewModel(
    private val database: AppDatabase,
    private val context: Context
) : ViewModel() {

    private val reminderManager = ClassReminderManager(context)
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    suspend fun prepareExportJson(): String {
        val classes = database.classDao().getAllClasses().first()
        val attendance = database.attendanceDao().getAllAttendance().first()
        val menu = database.menuDao().getAllMenu().first()
        val expenses = database.expenseDao().getAllExpenses().first()
        val settings = database.settingsDao().getSettingsSync()
        
        val exportData = ExportData(
            classes = classes,
            attendance = attendance,
            menu = menu,
            expenses = expenses,
            settings = settings
        )
        
        return json.encodeToString(exportData)
    }

    suspend fun importBackupJson(jsonString: String): Result<Unit> {
        return try {
            val backupData = json.decodeFromString<ExportData>(jsonString)
            
            database.withTransaction {
                // 1. Cancel existing alarms before wiping
                val existingClasses = database.classDao().getAllClasses().first()
                existingClasses.forEach { reminderManager.cancelAllClassAlarms(it) }

                // 2. Clear all tables
                database.clearAllTables()

                // 3. Re-insert data
                backupData.classes.forEach { database.classDao().insert(it) }
                backupData.attendance.forEach { database.attendanceDao().insert(it) }
                backupData.menu.forEach { database.menuDao().upsert(it) }
                backupData.expenses.forEach { database.expenseDao().insert(it) }
                
                backupData.settings?.let { 
                    database.settingsDao().upsertSettings(it)
                } ?: run {
                    database.settingsDao().upsertSettings(NotificationSettingsEntity())
                }
            }

            // 4. Re-schedule alarms with new data
            val newSettings = database.settingsDao().getSettingsSync()
            if (newSettings != null) {
                val newClasses = database.classDao().getAllClasses().first()
                newClasses.forEach { reminderManager.scheduleClassAlarms(it, newSettings) }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
