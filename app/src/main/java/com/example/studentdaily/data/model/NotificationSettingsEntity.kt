package com.example.studentdaily.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val classRemindersEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 30,
    val notifyClassStart: Boolean = false,
    val notifyClassEnd: Boolean = false,
    val dailyLimitEnabled: Boolean = false,
    val dailyLimit: Double = 0.0,
    val weeklyLimitEnabled: Boolean = false,
    val weeklyLimit: Double = 0.0,
    val monthlyLimitEnabled: Boolean = false,
    val monthlyLimit: Double = 0.0,
    val threshold50: Boolean = true,
    val threshold75: Boolean = true,
    val threshold90: Boolean = true,
    val threshold100: Boolean = true,
    val thresholdOver: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
