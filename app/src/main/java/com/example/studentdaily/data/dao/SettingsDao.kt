package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM notification_settings WHERE id = 1")
    fun getSettings(): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM notification_settings WHERE id = 1")
    suspend fun getSettingsSync(): NotificationSettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: NotificationSettingsEntity)
}
