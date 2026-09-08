package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.ClassEntity
import com.example.studentdaily.data.model.DayOfWeek
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Insert
    suspend fun insert(classEntity: ClassEntity): Long

    @Update
    suspend fun update(classEntity: ClassEntity)

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Query("SELECT * FROM classes")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE dayOfWeek = :day")
    fun getClassesForDay(day: DayOfWeek): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: Long): ClassEntity?
}
