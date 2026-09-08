package com.example.studentdaily.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.data.model.MenuEntity
import com.example.studentdaily.ui.viewmodel.MessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekMenuFormScreen(
    viewModel: MessViewModel,
    onBack: () -> Unit
) {
    val allMenu by viewModel.allMenu.collectAsState(initial = emptyList())
    
    // Local state to hold the 21 text values
    // Key is "Day_MealType"
    val menuState = remember { mutableStateMapOf<String, String>() }
    
    // Initialize state when data is loaded
    LaunchedEffect(allMenu) {
        val grouped = allMenu.groupBy { "${it.dayOfWeek}_${it.mealType}" }
        grouped.forEach { (key, menus) ->
            menuState[key] = menus.joinToString("\n") { it.itemsText }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Week Menu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val menusToSave = mutableListOf<Triple<DayOfWeek, MealType, String>>()
                        DayOfWeek.entries.forEach { day ->
                            MealType.entries.forEach { meal ->
                                val text = menuState["${day}_${meal}"] ?: ""
                                if (text.isNotBlank()) {
                                    menusToSave.add(Triple(day, meal, text))
                                }
                            }
                        }
                        viewModel.replaceFullWeekMenu(menusToSave)
                        onBack()
                    }) {
                        Text("SAVE ALL")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(DayOfWeek.entries) { day ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(day.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    MealType.entries.forEach { meal ->
                        val key = "${day}_${meal}"
                        OutlinedTextField(
                            value = menuState[key] ?: "",
                            onValueChange = { menuState[key] = it },
                            label = { Text(meal.name) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}
