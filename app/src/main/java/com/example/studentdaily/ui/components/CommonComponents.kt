package com.example.studentdaily.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.*
import com.example.studentdaily.ui.viewmodel.AttendanceStats
import com.example.studentdaily.ui.viewmodel.AttendanceViewModel
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassAttendanceItem(
    classEntity: ClassEntity,
    stats: AttendanceStats?,
    currentStatus: AttendanceStatus?,
    onMarkAttendance: (AttendanceStatus) -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(classEntity.subjectName, style = MaterialTheme.typography.titleLarge)
                    Text("${classEntity.startTime} - ${classEntity.endTime}", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Class", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", stats?.percentage ?: 0f),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onMarkAttendance(AttendanceStatus.ABSENT) },
                    colors = if (currentStatus == AttendanceStatus.ABSENT) 
                        ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("ABSENT", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onMarkAttendance(AttendanceStatus.PRESENT) },
                    colors = if (currentStatus == AttendanceStatus.PRESENT)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        else ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text("PRESENT")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MealCard(mealType: MealType, menus: List<MenuEntity>, onEdit: (List<MenuEntity>) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onEdit(menus) },
                onLongClick = { onEdit(menus) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mealType.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                IconButton(onClick = { onEdit(menus) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Menu", modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (menus.isNotEmpty()) {
                Column {
                    menus.forEach { menu ->
                        Text(
                            text = if (menus.size > 1) "• ${menu.itemsText}" else menu.itemsText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                Text("Not set", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseItem(expense: ExpenseEntity, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category.name, style = MaterialTheme.typography.labelSmall)
                Text(expense.note ?: "No note", style = MaterialTheme.typography.bodyLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.getDefault(), "₹%.2f", expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Expense", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = { content() }
    )
}

@Composable
fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discard changes?") },
        text = { Text("You have unsaved changes. Are you sure you want to discard them and go back?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Discard", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
