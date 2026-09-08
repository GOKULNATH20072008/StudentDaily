package com.example.studentdaily.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "menu")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val itemsText: String
)
