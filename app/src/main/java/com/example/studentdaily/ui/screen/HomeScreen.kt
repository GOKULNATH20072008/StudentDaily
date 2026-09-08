package com.example.studentdaily.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.ClassEntity
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.ExpenseEntity
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.data.model.MenuEntity
import com.example.studentdaily.ui.NavRoutes
import com.example.studentdaily.ui.components.*
import com.example.studentdaily.ui.viewmodel.AttendanceViewModel
import com.example.studentdaily.ui.viewmodel.ExpenseViewModel
import com.example.studentdaily.ui.viewmodel.MessViewModel
import com.example.studentdaily.ui.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    attendanceViewModel: AttendanceViewModel,
    messViewModel: MessViewModel,
    expenseViewModel: ExpenseViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigate: (String) -> Unit
) {
    val today = LocalDate.now()
    val dateString = today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    val dayString = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

    val classes by attendanceViewModel.todayClasses.collectAsState(initial = null)
    val menuItems by messViewModel.todayMenu.collectAsState(initial = null)
    val todayExpenses by expenseViewModel.todayExpenses.collectAsState(initial = null)
    val budgetStats by expenseViewModel.budgetStats.collectAsState(initial = null)
    
    val nextClass by attendanceViewModel.nextClass.collectAsState(initial = null)
    val settings by settingsViewModel.settings.collectAsState(initial = null)
    
    val allStats by attendanceViewModel.allAttendanceStats.collectAsState(initial = emptyMap())
    val todayAttendance by attendanceViewModel.todayAttendance.collectAsState(initial = emptyMap())

    var showAddMenu by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassEntity?>(null) }
    var editingMenuData by remember { mutableStateOf<Pair<List<MenuEntity>, Pair<DayOfWeek, MealType>>?>(null) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(dayString, style = MaterialTheme.typography.titleLarge)
                        Text(dateString, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(NavRoutes.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showAddMenu = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Filled.Add, "Quick Add")
                }
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Class") },
                        onClick = {
                            showAddMenu = false
                            onNavigate(NavRoutes.AddClass.route)
                        },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Menu") },
                        onClick = {
                            showAddMenu = false
                            onNavigate(NavRoutes.AddMenu.route)
                        },
                        leadingIcon = { Icon(Icons.Default.List, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Expense") },
                        onClick = {
                            showAddMenu = false
                            onNavigate(NavRoutes.AddExpense.route)
                        },
                        leadingIcon = { Icon(Icons.Default.ShoppingCart, null) }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Section
            item(key = "summary_section") {
                Spacer(modifier = Modifier.height(8.dp))
                
                if (nextClass != null || (settings != null && settings!!.dailyLimitEnabled)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (nextClass != null) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Next Class", style = MaterialTheme.typography.labelSmall)
                                    Text(nextClass!!.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(nextClass!!.startTime, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        if (settings != null && settings!!.dailyLimitEnabled) {
                            val spentToday = todayExpenses?.sumOf { it.amount } ?: 0.0
                            val percent = if (settings!!.dailyLimit > 0) (spentToday / settings!!.dailyLimit) * 100 else 0.0
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Daily Budget", style = MaterialTheme.typography.labelSmall)
                                    Text("₹${String.format(Locale.getDefault(), "%.0f", spentToday)} / ₹${String.format(Locale.getDefault(), "%.0f", settings!!.dailyLimit)}", 
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.getDefault(), "%.0f", percent)}% used", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Section 1: Classes
            item(key = "classes_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Today's Classes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    classes == null -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    classes!!.isEmpty() -> {
                        Column {
                            Text("No classes today", color = MaterialTheme.colorScheme.outline)
                            TextButton(onClick = { onNavigate(NavRoutes.AddClass.route) }) {
                                Text("+ Add your first class")
                            }
                        }
                    }
                }
            }
            if (classes != null) {
                items(classes!!, key = { "class_${it.id}" }) { classEntity ->
                    ClassAttendanceItem(
                        classEntity = classEntity,
                        stats = allStats[classEntity.id],
                        currentStatus = todayAttendance[classEntity.id],
                        onMarkAttendance = { attendanceViewModel.markAttendance(classEntity.id, it) },
                        onEdit = { editingClass = classEntity }
                    )
                }
            }

            // Section 2: Mess Menu
            item(key = "mess_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mess Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (menuItems == null) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (menuItems!!.isEmpty()) {
                    TextButton(onClick = { onNavigate(NavRoutes.WeekMenuForm.route) }) {
                        Text("+ Set up your weekly menu")
                    }
                }
            }
            if (menuItems != null) {
                items(MealType.entries, key = { "meal_${it.name}" }) { mealType ->
                    val menus = menuItems!!.filter { it.mealType == mealType }
                    MealCard(
                        mealType = mealType,
                        menus = menus,
                        onEdit = { editingMenuData = it to (DayOfWeek.valueOf(today.dayOfWeek.name.substring(0, 3).uppercase()) to mealType) }
                    )
                }
            }

            // Section 3: Expenses
            item(key = "expenses_header") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Expenses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (budgetStats == null) {
                    // Stats loading
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Spent: ₹${String.format(Locale.getDefault(), "%.2f", budgetStats!!.spentThisMonth)}", fontWeight = FontWeight.Bold)
                            Text("Avg: ₹${String.format(Locale.getDefault(), "%.2f", budgetStats!!.spentPerDay)}/day | ${budgetStats!!.daysRemaining} days left")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                when {
                    todayExpenses == null -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    todayExpenses!!.isEmpty() -> {
                        Column {
                            Text("No expenses logged today", color = MaterialTheme.colorScheme.outline)
                            TextButton(onClick = { onNavigate(NavRoutes.AddExpense.route) }) {
                                Text("+ Log an expense")
                            }
                        }
                    }
                }
            }
            if (todayExpenses != null) {
                items(todayExpenses!!, key = { "expense_${it.id}" }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = { editingExpense = expense }
                    )
                }
            }
            
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Dialogs
    editingClass?.let { entity ->
        EditClassDialog(
            classEntity = entity,
            onDismiss = { editingClass = null },
            onUpdate = { attendanceViewModel.updateClass(it) },
            onDelete = { attendanceViewModel.deleteClass(it) }
        )
    }

    editingMenuData?.let { data ->
        val entities = data.first
        val fallback = data.second
        MultiDishEditDialog(
            dishes = entities,
            initialDay = fallback.first,
            initialMeal = fallback.second,
            onDismiss = { editingMenuData = null },
            onSaveNewDish = { day, type, text -> messViewModel.saveMenu(day, type, text) },
            onUpdateDish = { messViewModel.updateMenu(it) },
            onDeleteDish = { messViewModel.deleteMenu(it) }
        )
    }

    editingExpense?.let { entity ->
        EditExpenseDialog(
            expenseEntity = entity,
            onDismiss = { editingExpense = null },
            onUpdate = { expenseViewModel.updateExpense(it) },
            onDelete = { expenseViewModel.deleteExpense(it) }
        )
    }
}
