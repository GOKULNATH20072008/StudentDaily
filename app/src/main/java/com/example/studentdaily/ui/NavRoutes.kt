package com.example.studentdaily.ui

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Planner : NavRoutes("planner")
    object Mess : NavRoutes("mess")
    object Expenses : NavRoutes("expenses")
    
    object AddClass : NavRoutes("add_class")
    object AddMenu : NavRoutes("add_menu")
    object AddExpense : NavRoutes("add_expense")
    object WeekMenuForm : NavRoutes("week_menu_form")
    object Settings : NavRoutes("settings")
}
