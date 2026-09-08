package com.example.studentdaily.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "expense_alert_state")
data class ExpenseAlertStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // DAILY, WEEKLY, MONTHLY
    val threshold: String, // 50, 75, 90, 100, OVER
    val periodKey: String // yyyy-MM-dd, yyyy-Www, yyyy-MM
)
