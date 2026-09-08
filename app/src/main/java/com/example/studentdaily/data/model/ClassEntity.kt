package com.example.studentdaily.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val dayOfWeek: DayOfWeek,
    val startTime: String, // "HH:mm"
    val endTime: String    // "HH:mm"
)
