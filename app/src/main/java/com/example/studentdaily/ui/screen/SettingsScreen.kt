package com.example.studentdaily.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studentdaily.ui.viewmodel.BackupViewModel
import com.example.studentdaily.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    backupViewModel: BackupViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val settings by settingsViewModel.settings.collectAsState(initial = null)
    var showResetDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf<String?>(null) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all app data?") },
            text = { Text("This will permanently delete your classes, attendance records, mess menus, expenses, notification settings, and expense alert history. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetAllData {
                            scope.launch {
                                snackbarHostState.showSnackbar("All app data has been reset.")
                            }
                        }
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = null },
            title = { Text("Import Backup?") },
            text = { Text("Importing this backup will replace your current StudentDaily data. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        val jsonString = showImportConfirmDialog!!
                        showImportConfirmDialog = null
                        scope.launch {
                            val result = backupViewModel.importBackupJson(jsonString)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Backup imported successfully.")
                            } else {
                                snackbarHostState.showSnackbar("Unable to import this backup. The file may be invalid or corrupted.")
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val json = backupViewModel.prepareExportJson()
                        context.contentResolver.openOutputStream(it)?.use { stream ->
                            stream.write(json.toByteArray())
                        }
                        snackbarHostState.showSnackbar("Data exported successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val jsonString = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(it)?.use { stream ->
                                stream.bufferedReader().readText()
                            }
                        }
                        if (jsonString != null) {
                            showImportConfirmDialog = jsonString
                        } else {
                            snackbarHostState.showSnackbar("Failed to read file.")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error reading file: ${e.message}")
                    }
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        if (settings == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section: Class Notifications
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Class Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Class Reminders", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings!!.classRemindersEnabled,
                            onCheckedChange = { settingsViewModel.updateSettings(settings!!.copy(classRemindersEnabled = it)) }
                        )
                    }

                    if (settings!!.classRemindersEnabled) {
                        Text("Reminder Interval", style = MaterialTheme.typography.labelLarge)
                        val intervals = listOf(5, 10, 15, 30, 60)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            intervals.forEach { min ->
                                FilterChip(
                                    selected = settings!!.reminderMinutesBefore == min,
                                    onClick = { settingsViewModel.updateSettings(settings!!.copy(reminderMinutesBefore = min)) },
                                    label = { Text("${min}m") }
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notify when class starts", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings!!.notifyClassStart,
                            onCheckedChange = { settingsViewModel.updateSettings(settings!!.copy(notifyClassStart = it)) }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notify when class ends", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings!!.notifyClassEnd,
                            onCheckedChange = { settingsViewModel.updateSettings(settings!!.copy(notifyClassEnd = it)) }
                        )
                    }
                }

                HorizontalDivider()

                // Section: Expense Limits
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Expense Limits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                    LimitInput("Daily Limit", settings!!.dailyLimitEnabled, settings!!.dailyLimit, 
                        onToggle = { settingsViewModel.updateSettings(settings!!.copy(dailyLimitEnabled = it)) },
                        onValueChange = { settingsViewModel.updateSettings(settings!!.copy(dailyLimit = it)) }
                    )

                    LimitInput("Weekly Limit", settings!!.weeklyLimitEnabled, settings!!.weeklyLimit, 
                        onToggle = { settingsViewModel.updateSettings(settings!!.copy(weeklyLimitEnabled = it)) },
                        onValueChange = { settingsViewModel.updateSettings(settings!!.copy(weeklyLimit = it)) }
                    )

                    LimitInput("Monthly Limit", settings!!.monthlyLimitEnabled, settings!!.monthlyLimit, 
                        onToggle = { settingsViewModel.updateSettings(settings!!.copy(monthlyLimitEnabled = it)) },
                        onValueChange = { settingsViewModel.updateSettings(settings!!.copy(monthlyLimit = it)) }
                    )

                    Text("Alert Thresholds", style = MaterialTheme.typography.titleMedium)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThresholdChip("50%", settings!!.threshold50) { settingsViewModel.updateSettings(settings!!.copy(threshold50 = it)) }
                        ThresholdChip("75%", settings!!.threshold75) { settingsViewModel.updateSettings(settings!!.copy(threshold75 = it)) }
                        ThresholdChip("90%", settings!!.threshold90) { settingsViewModel.updateSettings(settings!!.copy(threshold90 = it)) }
                        ThresholdChip("100%", settings!!.threshold100) { settingsViewModel.updateSettings(settings!!.copy(threshold100 = it)) }
                        ThresholdChip("Over Limit", settings!!.thresholdOver) { settingsViewModel.updateSettings(settings!!.copy(thresholdOver = it)) }
                    }
                }

                HorizontalDivider()

                // Section: Notification Preferences
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notification Preferences", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sound", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings!!.soundEnabled,
                            onCheckedChange = { settingsViewModel.updateSettings(settings!!.copy(soundEnabled = it)) }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Vibration", modifier = Modifier.weight(1f))
                        Switch(
                            checked = settings!!.vibrationEnabled,
                            onCheckedChange = { settingsViewModel.updateSettings(settings!!.copy(vibrationEnabled = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: These app-level settings attempt to override system defaults. For precise control, use System Notification Settings.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("System Notification Settings")
                    }
                }

                HorizontalDivider()

                // Section: Data
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Manage your backup and local data.", style = MaterialTheme.typography.bodyMedium)
                    
                    Button(
                        onClick = {
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                            exportLauncher.launch("student_daily_backup_$timestamp.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export Data")
                    }

                    Button(
                        onClick = {
                            importLauncher.launch("application/json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import Backup")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset All App Data")
                    }
                }
            }
        }
    }
}

@Composable
fun LimitInput(label: String, enabled: Boolean, value: Double, onToggle: (Boolean) -> Unit, onValueChange: (Double) -> Unit) {
    var textValue by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        if (enabled) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { 
                    textValue = it
                    it.toDoubleOrNull()?.let { v -> if (v >= 0) onValueChange(v) }
                },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThresholdChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(label) }
    )
}
