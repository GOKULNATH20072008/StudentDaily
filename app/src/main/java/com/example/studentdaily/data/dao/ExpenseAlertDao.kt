package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.ExpenseAlertStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseAlertDao {
    @Query("SELECT * FROM expense_alert_state WHERE type = :type AND threshold = :threshold AND periodKey = :periodKey LIMIT 1")
    suspend fun getAlertState(type: String, threshold: String, periodKey: String): ExpenseAlertStateEntity?

    @Insert
    suspend fun insertAlertState(alertState: ExpenseAlertStateEntity)

    @Query("DELETE FROM expense_alert_state")
    suspend fun clearAll()
}
