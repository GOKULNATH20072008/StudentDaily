package com.example.studentdaily.data.dao

import androidx.room.*
import com.example.studentdaily.data.model.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date = :date")
    fun getExpensesForDate(date: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate")
    fun getExpensesInRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE date LIKE :yearMonth || '%'")
    fun getExpensesForMonth(yearMonth: String): Flow<List<ExpenseEntity>>
}
