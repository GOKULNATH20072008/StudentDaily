package com.example.studentdaily.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentdaily.ClassReminderManager
import com.example.studentdaily.data.dao.AttendanceDao
import com.example.studentdaily.data.dao.ClassDao
import com.example.studentdaily.data.dao.SettingsDao
import com.example.studentdaily.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

data class AttendanceStats(val presentCount: Int, val absentCount: Int) {
    val totalCount: Int get() = presentCount + absentCount
    val percentage: Float get() = if (totalCount == 0) 0f else (presentCount.toFloat() / totalCount) * 100
}

class AttendanceViewModel(
    private val classDao: ClassDao,
    private val attendanceDao: AttendanceDao,
    private val settingsDao: SettingsDao,
    context: Context
) : ViewModel() {
    private val reminderManager = ClassReminderManager(context)

    private fun getTodayDayOfWeek(): DayOfWeek {
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
        return try {
            DayOfWeek.valueOf(today)
        } catch (e: Exception) {
            DayOfWeek.MON // Fallback
        }
    }

    val todayClasses: Flow<List<ClassEntity>> = classDao.getClassesForDay(getTodayDayOfWeek())

    val nextClass: Flow<ClassEntity?> = todayClasses.map { list ->
        val now = LocalTime.now()
        list.filter { 
            LocalTime.parse(it.startTime).isAfter(now)
        }.minByOrNull { it.startTime }
    }

    val allClasses: Flow<List<ClassEntity>> = classDao.getAllClasses()

    val todayAttendance: Flow<Map<Long, AttendanceStatus>> = attendanceDao.getAttendanceForDate(LocalDate.now().toString()).map { list ->
        list.associate { it.classId to it.status }
    }

    val allAttendanceStats: Flow<Map<Long, AttendanceStats>> = attendanceDao.getAllAttendance().map { list ->
        list.groupBy { it.classId }.mapValues { entry ->
            val present = entry.value.count { it.status == AttendanceStatus.PRESENT }
            val absent = entry.value.count { it.status == AttendanceStatus.ABSENT }
            AttendanceStats(present, absent)
        }
    }

    fun getAttendanceStats(classId: Long): Flow<AttendanceStats> {
        return attendanceDao.getAttendanceForClass(classId).map { attendanceList ->
            val present = attendanceList.count { it.status == AttendanceStatus.PRESENT }
            val absent = attendanceList.count { it.status == AttendanceStatus.ABSENT }
            AttendanceStats(present, absent)
        }
    }

    fun markAttendance(classId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            val todayDate = LocalDate.now().toString()
            val existing = attendanceDao.getAttendanceForClassOnDate(classId, todayDate)
            
            if (existing != null) {
                if (existing.status == status) {
                    // Toggle off if tapping the same status
                    attendanceDao.delete(existing)
                } else {
                    // Update if tapping the other status
                    attendanceDao.update(existing.copy(status = status))
                }
            } else {
                // Insert new record
                val entity = AttendanceEntity(
                    classId = classId,
                    date = todayDate,
                    status = status
                )
                attendanceDao.insert(entity)
            }
        }
    }

    fun updateClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            classDao.update(classEntity)
            val settings = settingsDao.getSettingsSync() ?: NotificationSettingsEntity()
            reminderManager.scheduleClassAlarms(classEntity, settings)
        }
    }

    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            classDao.delete(classEntity)
            reminderManager.cancelAllClassAlarms(classEntity)
        }
    }

    fun saveClass(subjectName: String, dayOfWeek: DayOfWeek, startTime: String, endTime: String) {
        viewModelScope.launch {
            val entity = ClassEntity(
                subjectName = subjectName,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime
            )
            val id = classDao.insert(entity)
            val newEntity = entity.copy(id = id)
            val settings = settingsDao.getSettingsSync() ?: NotificationSettingsEntity()
            reminderManager.scheduleClassAlarms(newEntity, settings)
        }
    }
}
