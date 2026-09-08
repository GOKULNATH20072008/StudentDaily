package com.example.studentdaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val classes: List<ClassEntity>,
    val attendance: List<AttendanceEntity>,
    val menu: List<MenuEntity>,
    val expenses: List<ExpenseEntity>,
    val settings: NotificationSettingsEntity? = null
)
