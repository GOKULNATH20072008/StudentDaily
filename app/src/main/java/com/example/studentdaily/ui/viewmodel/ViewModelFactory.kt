package com.example.studentdaily.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentdaily.data.AppDatabase

class ViewModelFactory(
    private val database: AppDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> {
                AttendanceViewModel(database.classDao(), database.attendanceDao(), database.settingsDao(), context) as T
            }
            modelClass.isAssignableFrom(MessViewModel::class.java) -> {
                MessViewModel(database.menuDao()) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(database.expenseDao(), database.settingsDao(), database.expenseAlertDao(), context) as T
            }
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> {
                BackupViewModel(database, context) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(database, database.settingsDao(), database.classDao(), context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
