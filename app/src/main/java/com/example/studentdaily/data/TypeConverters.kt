package com.example.studentdaily.data

import androidx.room.TypeConverter
import com.example.studentdaily.data.model.AttendanceStatus
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.ExpenseCategory
import com.example.studentdaily.data.model.MealType

class AppTypeConverters {
    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)

    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType = MealType.valueOf(value)

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory): String = value.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
