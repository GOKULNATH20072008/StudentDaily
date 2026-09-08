package com.example.studentdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentdaily.data.dao.MenuDao
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.data.model.MenuEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MessViewModel(private val menuDao: MenuDao) : ViewModel() {

    private fun getTodayDayOfWeek(): DayOfWeek {
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
        return try {
            DayOfWeek.valueOf(today)
        } catch (e: Exception) {
            DayOfWeek.MON
        }
    }

    val todayMenu: Flow<List<MenuEntity>> = menuDao.getMenuForDay(getTodayDayOfWeek())

    val allMenu: Flow<List<MenuEntity>> = menuDao.getAllMenu()

    fun updateMenu(menu: MenuEntity) {
        viewModelScope.launch {
            menuDao.upsert(menu)
        }
    }

    fun deleteMenu(menu: MenuEntity) {
        viewModelScope.launch {
            menuDao.delete(menu)
        }
    }

    fun saveMenu(dayOfWeek: DayOfWeek, mealType: MealType, itemsText: String) {
        viewModelScope.launch {
            // Split by lines to allow adding multiple dishes at once if user uses newlines
            val dishes = itemsText.split("\n").filter { it.isNotBlank() }
            dishes.forEach { dish ->
                val entity = MenuEntity(
                    dayOfWeek = dayOfWeek,
                    mealType = mealType,
                    itemsText = dish.trim()
                )
                menuDao.upsert(entity)
            }
        }
    }

    fun replaceFullWeekMenu(menus: List<Triple<DayOfWeek, MealType, String>>) {
        viewModelScope.launch {
            menuDao.deleteAll()
            val entities = mutableListOf<MenuEntity>()
            menus.forEach { (day, type, text) ->
                // Each line becomes a dish
                text.split("\n").filter { it.isNotBlank() }.forEach { dish ->
                    entities.add(MenuEntity(dayOfWeek = day, mealType = type, itemsText = dish.trim()))
                }
            }
            if (entities.isNotEmpty()) {
                menuDao.upsertAll(entities)
            }
        }
    }
}
