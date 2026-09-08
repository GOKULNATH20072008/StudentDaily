package com.example.studentdaily.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.ui.components.DiscardChangesDialog
import com.example.studentdaily.ui.viewmodel.MessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuScreen(viewModel: MessViewModel, onBack: () -> Unit) {
    var dayOfWeek by remember { mutableStateOf(DayOfWeek.MON) }
    var mealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var itemsText by remember { mutableStateOf("") }
    
    var dayExpanded by remember { mutableStateOf(false) }
    var mealExpanded by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasChanges = itemsText.isNotBlank() || dayOfWeek != DayOfWeek.MON || mealType != MealType.BREAKFAST

    fun handleBack() {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = ::handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Menu Item") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = dayExpanded,
                onExpandedChange = { dayExpanded = !dayExpanded }
            ) {
                OutlinedTextField(
                    value = dayOfWeek.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Day of Week") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dayExpanded,
                    onDismissRequest = { dayExpanded = false }
                ) {
                    DayOfWeek.entries.forEach { day ->
                        DropdownMenuItem(
                            text = { Text(day.name) },
                            onClick = {
                                dayOfWeek = day
                                dayExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = mealExpanded,
                onExpandedChange = { mealExpanded = !mealExpanded }
            ) {
                OutlinedTextField(
                    value = mealType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Meal Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealExpanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = mealExpanded,
                    onDismissRequest = { mealExpanded = false }
                ) {
                    MealType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                mealType = type
                                mealExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = itemsText,
                onValueChange = { itemsText = it },
                label = { Text("Menu Items") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    viewModel.saveMenu(dayOfWeek, mealType, itemsText)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = itemsText.isNotBlank()
            ) {
                Text("Save Menu")
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = { showDiscardDialog = false },
            onConfirm = {
                showDiscardDialog = false
                onBack()
            }
        )
    }
}
