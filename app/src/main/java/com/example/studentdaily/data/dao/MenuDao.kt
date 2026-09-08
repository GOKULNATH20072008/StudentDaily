package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.data.model.MenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Upsert
    suspend fun upsert(menu: MenuEntity)

    @Upsert
    suspend fun upsertAll(menus: List<MenuEntity>)

    @Delete
    suspend fun delete(menu: MenuEntity)

    @Query("SELECT * FROM menu")
    fun getAllMenu(): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menu WHERE dayOfWeek = :day")
    fun getMenuForDay(day: DayOfWeek): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menu WHERE dayOfWeek = :day AND mealType = :type LIMIT 1")
    suspend fun getMenuSync(day: DayOfWeek, type: MealType): MenuEntity?

    @Query("DELETE FROM menu")
    suspend fun deleteAll()
}
