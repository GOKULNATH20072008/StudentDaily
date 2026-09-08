package com.example.studentdaily.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class DayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN
}

@Serializable
enum class AttendanceStatus {
    PRESENT, ABSENT
}

@Serializable
enum class MealType {
    BREAKFAST, LUNCH, DINNER
}

@Serializable
enum class ExpenseCategory {
    FOOD, MESS, TRANSPORT, OTHER
}
