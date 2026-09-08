package com.example.studentdaily

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studentdaily.data.AppDatabase
import com.example.studentdaily.ui.NavRoutes
import com.example.studentdaily.ui.screen.*
import com.example.studentdaily.ui.theme.StudentdailyTheme
import com.example.studentdaily.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }

        enableEdgeToEdge()
        setContent {
            StudentdailyTheme {
                val context = this
                val database = remember { AppDatabase.getInstance(context) }
                val factory = remember { ViewModelFactory(database, context.applicationContext) }

                val attendanceViewModel: AttendanceViewModel = viewModel(factory = factory)
                val messViewModel: MessViewModel = viewModel(factory = factory)
                val expenseViewModel: ExpenseViewModel = viewModel(factory = factory)
                val backupViewModel: BackupViewModel = viewModel(factory = factory)
                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

                val navController = rememberNavController()

                val tabs = listOf(
                    TabItem("Home", NavRoutes.Home.route, Icons.Default.Home),
                    TabItem("Planner", NavRoutes.Planner.route, Icons.Default.DateRange),
                    TabItem("Mess", NavRoutes.Mess.route, Icons.AutoMirrored.Filled.List),
                    TabItem("Expenses", NavRoutes.Expenses.route, Icons.Default.ShoppingCart)
                )

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Only show bottom bar on main tabs
                        val showBottomBar = tabs.any { tab -> 
                            currentDestination?.hierarchy?.any { it.route == tab.route } == true 
                        }

                        if (showBottomBar) {
                            NavigationBar {
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.Home.route) {
                            HomeScreen(
                                attendanceViewModel = attendanceViewModel,
                                messViewModel = messViewModel,
                                expenseViewModel = expenseViewModel,
                                settingsViewModel = settingsViewModel,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }
                        composable(NavRoutes.Planner.route) {
                            PlannerScreen(
                                viewModel = attendanceViewModel,
                                onAddClass = { navController.navigate(NavRoutes.AddClass.route) }
                            )
                        }
                        composable(NavRoutes.Mess.route) {
                            MessScreen(
                                viewModel = messViewModel,
                                onAddMenu = { navController.navigate(NavRoutes.AddMenu.route) },
                                onEditFullWeek = { navController.navigate(NavRoutes.WeekMenuForm.route) }
                            )
                        }
                        composable(NavRoutes.Expenses.route) {
                            ExpensesScreen(
                                viewModel = expenseViewModel,
                                onAddExpense = { navController.navigate(NavRoutes.AddExpense.route) }
                            )
                        }
                        
                        // Quick Add Screens
                        composable(NavRoutes.AddClass.route) {
                            AddClassScreen(viewModel = attendanceViewModel, onBack = { navController.popBackStack() })
                        }
                        composable(NavRoutes.AddMenu.route) {
                            AddMenuScreen(viewModel = messViewModel, onBack = { navController.popBackStack() })
                        }
                        composable(NavRoutes.AddExpense.route) {
                            AddExpenseScreen(viewModel = expenseViewModel, onBack = { navController.popBackStack() })
                        }
                        composable(NavRoutes.WeekMenuForm.route) {
                            WeekMenuFormScreen(viewModel = messViewModel, onBack = { navController.popBackStack() })
                        }
                        composable(NavRoutes.Settings.route) {
                            SettingsScreen(
                                backupViewModel = backupViewModel,
                                settingsViewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(val label: String, val route: String, val icon: ImageVector)
