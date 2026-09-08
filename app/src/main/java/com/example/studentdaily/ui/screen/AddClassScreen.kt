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
import com.example.studentdaily.ui.components.DiscardChangesDialog
import com.example.studentdaily.ui.components.TimePickerDialog
import com.example.studentdaily.ui.viewmodel.AttendanceViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(viewModel: AttendanceViewModel, onBack: () -> Unit) {
    val initialDay = remember {
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
        try { DayOfWeek.valueOf(today) } catch (e: Exception) { DayOfWeek.MON }
    }
    var subjectName by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(initialDay) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val startTimeState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    val endTimeState = rememberTimePickerState(initialHour = 10, initialMinute = 0)
    
    val startTime = remember(startTimeState.hour, startTimeState.minute) {
        String.format(Locale.getDefault(), "%02d:%02d", startTimeState.hour, startTimeState.minute)
    }
    val endTime = remember(endTimeState.hour, endTimeState.minute) {
        String.format(Locale.getDefault(), "%02d:%02d", endTimeState.hour, endTimeState.minute)
    }
    
    var expanded by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasChanges = subjectName.isNotBlank() || dayOfWeek != initialDay

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
                title = { Text("Add New Class") },
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

            Button(
                onClick = {
                    viewModel.saveClass(subjectName, dayOfWeek, startTime, endTime)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = subjectName.isNotBlank()
            ) {
                Text("Save Class")
            }
        }
    }

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
