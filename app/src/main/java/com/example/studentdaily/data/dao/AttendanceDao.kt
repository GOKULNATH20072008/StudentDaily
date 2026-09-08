package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert
    suspend fun insert(attendance: AttendanceEntity): Long

    @Update
    suspend fun update(attendance: AttendanceEntity)

    @Delete
    suspend fun delete(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId")
    fun getAttendanceForClass(classId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date LIMIT 1")
    suspend fun getAttendanceForClassOnDate(classId: Long, date: String): AttendanceEntity?

    @Query("DELETE FROM attendance WHERE classId = :classId AND date = :date")
    suspend fun deleteAttendanceForClassOnDate(classId: Long, date: String)

    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>
}
