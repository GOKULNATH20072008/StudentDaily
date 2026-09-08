package com.example.studentdaily.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentdaily.NotificationHelper
import com.example.studentdaily.data.dao.ExpenseAlertDao
import com.example.studentdaily.data.dao.ExpenseDao
import com.example.studentdaily.data.dao.SettingsDao
import com.example.studentdaily.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class BudgetStats(
    val spentThisMonth: Double,
    val spentPerDay: Double,
    val daysRemaining: Int
)

class ExpenseViewModel(
    private val expenseDao: ExpenseDao,
    private val settingsDao: SettingsDao,
    private val expenseAlertDao: ExpenseAlertDao,
    context: Context
) : ViewModel() {
    private val notificationHelper = NotificationHelper(context)
    private val todayDate = LocalDate.now().toString()
    private val currentYearMonth = YearMonth.now().toString()

    val todayExpenses: Flow<List<ExpenseEntity>> = expenseDao.getExpensesForDate(todayDate)

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses().map { list ->
        list.sortedByDescending { it.date }
    }

    val monthlyTotal: Flow<Double> = expenseDao.getExpensesForMonth(currentYearMonth).map { expenses ->
        expenses.sumOf { it.amount }
    }

    val weeklyExpenses: Flow<List<ExpenseEntity>> = flow {
        val now = LocalDate.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
        val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString()
        emitAll(expenseDao.getExpensesInRange(startOfWeek, endOfWeek))
    }

    val weeklyTotalByCategory: Flow<Map<ExpenseCategory, Double>> = weeklyExpenses.map { expenses ->
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val budgetStats: Flow<BudgetStats> = monthlyTotal.map { total ->
        val now = LocalDate.now()
        val daysElapsed = now.dayOfMonth
        val daysInMonth = YearMonth.now().lengthOfMonth()
        
        BudgetStats(
            spentThisMonth = total,
            spentPerDay = if (daysElapsed == 0) total else total / daysElapsed,
            daysRemaining = daysInMonth - daysElapsed
        )
    }

    private fun checkLimits() {
        viewModelScope.launch {
            val settings = settingsDao.getSettingsSync() ?: return@launch
            val now = LocalDate.now()
            
            if (settings.dailyLimitEnabled) {
                val dailyTotal = expenseDao.getExpensesForDate(now.toString()).first().sumOf { it.amount }
                checkThresholds(dailyTotal, settings.dailyLimit, "DAILY", now.toString(), settings)
            }

            if (settings.weeklyLimitEnabled) {
                val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val weeklyTotal = expenseDao.getExpensesInRange(startOfWeek.toString(), endOfWeek.toString()).first().sumOf { it.amount }
                val weekKey = "${now.year}-W${now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
                checkThresholds(weeklyTotal, settings.weeklyLimit, "WEEKLY", weekKey, settings)
            }

            if (settings.monthlyLimitEnabled) {
                val monthlyTotal = expenseDao.getExpensesForMonth(YearMonth.now().toString()).first().sumOf { it.amount }
                checkThresholds(monthlyTotal, settings.monthlyLimit, "MONTHLY", YearMonth.now().toString(), settings)
            }
        }
    }

    private suspend fun checkThresholds(total: Double, limit: Double, type: String, periodKey: String, settings: NotificationSettingsEntity) {
        if (limit <= 0) return
        
        val percent = (total / limit) * 100
        val thresholds = listOf(
            Triple(50.0, "50", settings.threshold50),
            Triple(75.0, "75", settings.threshold75),
            Triple(90.0, "90", settings.threshold90),
            Triple(100.0, "100", settings.threshold100),
            Triple(100.01, "OVER", settings.thresholdOver)
        )

        for ((thresholdValue, label, isEnabled) in thresholds) {
            if (isEnabled && percent >= thresholdValue) {
                val alreadyTriggered = expenseAlertDao.getAlertState(type, label, periodKey) != null
                if (!alreadyTriggered) {
                    val title = "$type Expense Alert"
                    val content = when(label) {
                        "OVER" -> "$type expense limit exceeded by ₹${String.format("%.2f", total - limit)}."
                        "100" -> "$type expense limit reached."
                        else -> "You have used $label% of your ₹${String.format("%.2f", limit)} $type limit."
                    }
                    notificationHelper.showNotification(
                        NotificationHelper.EXPENSE_ALERTS_CHANNEL_ID,
                        title,
                        content,
                        type.hashCode() + label.hashCode(),
                        soundEnabled = settings.soundEnabled,
                        vibrationEnabled = settings.vibrationEnabled
                    )
                    expenseAlertDao.insertAlertState(ExpenseAlertStateEntity(type = type, threshold = label, periodKey = periodKey))
                }
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.update(expense)
            checkLimits()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseDao.delete(expense)
            // We don't usually notify on delete, but we could recalculate if we wanted to allow crossing thresholds AGAIN.
            // Requirement says "do NOT repeatedly send notifications simply because the percentage changes".
        }
    }

    fun saveExpense(amount: Double, category: ExpenseCategory, date: String, note: String?) {
        viewModelScope.launch {
            val entity = ExpenseEntity(
                amount = amount,
                category = category,
                date = date,
                note = note
            )
            expenseDao.insert(entity)
            checkLimits()
        }
    }
}
