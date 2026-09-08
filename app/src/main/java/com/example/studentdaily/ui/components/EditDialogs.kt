package com.example.studentdaily.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClassDialog(
    classEntity: ClassEntity,
    onDismiss: () -> Unit,
    onUpdate: (ClassEntity) -> Unit,
    onDelete: (ClassEntity) -> Unit
) {
    var subjectName by remember { mutableStateOf(classEntity.subjectName) }
    var dayOfWeek by remember { mutableStateOf(classEntity.dayOfWeek) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    // Safe parsing of time strings
    val startParts = remember(classEntity.startTime) {
        val parts = classEntity.startTime.split(":")
        if (parts.size == 2) {
            parts[0].toIntOrNull() to parts[1].toIntOrNull()
        } else null
    }
    val endParts = remember(classEntity.endTime) {
        val parts = classEntity.endTime.split(":")
        if (parts.size == 2) {
            parts[0].toIntOrNull() to parts[1].toIntOrNull()
        } else null
    }
    
    val startTimeState = rememberTimePickerState(
        initialHour = startParts?.first ?: 9, 
        initialMinute = startParts?.second ?: 0
    )
    val endTimeState = rememberTimePickerState(
        initialHour = endParts?.first ?: 10, 
        initialMinute = endParts?.second ?: 0
    )
    
    val startTime = remember(startTimeState.hour, startTimeState.minute) {
        String.format(Locale.getDefault(), "%02d:%02d", startTimeState.hour, startTimeState.minute)
    }
    val endTime = remember(endTimeState.hour, endTimeState.minute) {
        String.format(Locale.getDefault(), "%02d:%02d", endTimeState.hour, endTimeState.minute)
    }
    
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Class") },
            text = { Text("Are you sure you want to delete '${classEntity.subjectName}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(classEntity)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Class") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = dayOfWeek.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of Week") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day.name) },
                                onClick = {
                                    dayOfWeek = day
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = startTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Time") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showStartTimePicker = true }) {
                            Text("🕒")
                        }
                    }
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Time") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showEndTimePicker = true }) {
                            Text("🕒")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdate(classEntity.copy(
                    subjectName = subjectName,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                ))
                onDismiss()
            }) { Text("Update") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    showDeleteConfirm = true
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onConfirm = { showStartTimePicker = false }
        ) {
            TimePicker(state = startTimeState)
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onConfirm = { showEndTimePicker = false }
        ) {
            TimePicker(state = endTimeState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiDishEditDialog(
    dishes: List<MenuEntity>,
    initialDay: DayOfWeek,
    initialMeal: MealType,
    onDismiss: () -> Unit,
    onSaveNewDish: (DayOfWeek, MealType, String) -> Unit,
    onUpdateDish: (MenuEntity) -> Unit,
    onDeleteDish: (MenuEntity) -> Unit
) {
    var newDishText by remember { mutableStateOf("") }
    var editingDish by remember { mutableStateOf<MenuEntity?>(null) }
    var dishToEditValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${initialDay.name} - ${initialMeal.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dishes.isNotEmpty()) {
                    Text("Current Dishes", style = MaterialTheme.typography.titleMedium)
                    dishes.forEach { dish ->
                        if (editingDish?.id == dish.id) {
                            OutlinedTextField(
                                value = dishToEditValue,
                                onValueChange = { dishToEditValue = it },
                                label = { Text("Edit Dish") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { 
                                            onUpdateDish(dish.copy(itemsText = dishToEditValue))
                                            editingDish = null
                                        }) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                        IconButton(onClick = { editingDish = null }) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                }
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(dish.itemsText, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { 
                                        editingDish = dish
                                        dishToEditValue = dish.itemsText
                                    }) {
                                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onDeleteDish(dish) }) {
                                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Text("Add New Dish", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = newDishText,
                    onValueChange = { newDishText = it },
                    label = { Text("Dish Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newDishText.isNotBlank()) {
                            onSaveNewDish(initialDay, initialMeal, newDishText)
                            newDishText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = newDishText.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMenuDialog(
    menuEntity: MenuEntity?,
    initialDay: DayOfWeek,
    initialMeal: MealType,
    onDismiss: () -> Unit,
    onSave: (DayOfWeek, MealType, String) -> Unit,
    onUpdate: (MenuEntity) -> Unit,
    onDelete: (MenuEntity) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(menuEntity?.dayOfWeek ?: initialDay) }
    var mealType by remember { mutableStateOf(menuEntity?.mealType ?: initialMeal) }
    var itemsText by remember { mutableStateOf(menuEntity?.itemsText ?: "") }
    
    var dayExpanded by remember { mutableStateOf(false) }
    var mealExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && menuEntity != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Menu Item") },
            text = { Text("Are you sure you want to delete this ${menuEntity.mealType} entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(menuEntity)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (menuEntity == null) "Add Menu" else "Edit Menu") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
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
            }
        },
        confirmButton = {
            Button(onClick = {
                if (menuEntity == null) {
                    onSave(dayOfWeek, mealType, itemsText)
                } else {
                    onUpdate(menuEntity.copy(dayOfWeek = dayOfWeek, mealType = mealType, itemsText = itemsText))
                }
                onDismiss()
            }) { Text(if (menuEntity == null) "Save" else "Update") }
        },
        dismissButton = {
            Row {
                if (menuEntity != null) {
                    TextButton(onClick = {
                        showDeleteConfirm = true
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expenseEntity: ExpenseEntity,
    onDismiss: () -> Unit,
    onUpdate: (ExpenseEntity) -> Unit,
    onDelete: (ExpenseEntity) -> Unit
) {
    var amount by remember { mutableStateOf(expenseEntity.amount.toString()) }
    var category by remember { mutableStateOf(expenseEntity.category) }
    var note by remember { mutableStateOf(expenseEntity.note ?: "") }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val initialDate = remember { dateFormatter.parse(expenseEntity.date) ?: Date() }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time
    )
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectedDate = remember(datePickerState.selectedDateMillis) {
        val date = datePickerState.selectedDateMillis?.let { Date(it) } ?: Date()
        dateFormatter.format(date)
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense of ₹${expenseEntity.amount}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(expenseEntity)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        ExpenseCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Text("📅")
                        }
                    }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                onUpdate(expenseEntity.copy(
                    amount = amountDouble,
                    category = category,
                    date = selectedDate,
                    note = note.ifBlank { null }
                ))
                onDismiss()
            }) { Text("Update") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    showDeleteConfirm = true
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
