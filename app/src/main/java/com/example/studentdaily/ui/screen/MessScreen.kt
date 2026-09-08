package com.example.studentdaily.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.data.model.MealType
import com.example.studentdaily.data.model.MenuEntity
import com.example.studentdaily.ui.components.MealCard
import com.example.studentdaily.ui.components.MultiDishEditDialog
import com.example.studentdaily.ui.viewmodel.MessViewModel

enum class MessMode {
    Weekly, Timetable
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessScreen(
    viewModel: MessViewModel,
    onAddMenu: () -> Unit,
    onEditFullWeek: () -> Unit
) {
    val allMenu by viewModel.allMenu.collectAsState(initial = null)
    // Now we need to pass the list of entities for a specific day/meal to the dialog
    var editingMenuData by remember { mutableStateOf<Pair<List<MenuEntity>, Pair<DayOfWeek, MealType>>?>(null) }
    var mode by remember { mutableStateOf(MessMode.Weekly) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mess Menu") },
                    actions = {
                        IconButton(onClick = onEditFullWeek) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Full Week")
                        }
                        IconButton(onClick = onAddMenu) {
                            Icon(Icons.Default.Add, contentDescription = "Add Meal")
                        }
                    },
                    windowInsets = TopAppBarDefaults.windowInsets
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    MessMode.entries.forEachIndexed { index, messMode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = MessMode.entries.size),
                            onClick = { mode = messMode },
                            selected = mode == messMode
                        ) {
                            Text(messMode.name)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (mode) {
                MessMode.Weekly -> {
                    WeeklyMessListView(
                        allMenu = allMenu,
                        onEditFullWeek = onEditFullWeek,
                        onEdit = { editingMenuData = it }
                    )
                }
                MessMode.Timetable -> {
                    MessTimetableView(
                        allMenu = allMenu ?: emptyList(),
                        onEdit = { editingMenuData = it }
                    )
                }
            }
        }
    }

    editingMenuData?.let { data ->
        val entities = data.first
        val fallback = data.second
        MultiDishEditDialog(
            dishes = entities,
            initialDay = fallback.first,
            initialMeal = fallback.second,
            onDismiss = { editingMenuData = null },
            onSaveNewDish = { day, type, text -> viewModel.saveMenu(day, type, text) },
            onUpdateDish = { viewModel.updateMenu(it) },
            onDeleteDish = { viewModel.deleteMenu(it) }
        )
    }
}

@Composable
fun WeeklyMessListView(
    allMenu: List<MenuEntity>?,
    onEditFullWeek: () -> Unit,
    onEdit: (Pair<List<MenuEntity>, Pair<DayOfWeek, MealType>>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Weekly Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (allMenu == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (allMenu.isEmpty()) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("No menu items set.", color = MaterialTheme.colorScheme.outline)
                    Button(onClick = onEditFullWeek) {
                        Text("Set up weekly menu")
                    }
                }
            }
        } else {
            DayOfWeek.entries.forEach { day ->
                item(key = day.name) {
                    Text(day.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                items(MealType.entries, key = { "${day.name}_${it.name}" }) { mealType ->
                    val menus = allMenu.filter { it.dayOfWeek == day && it.mealType == mealType }
                    MealCard(
                        mealType = mealType,
                        menus = menus,
                        onEdit = { onEdit(menus to (day to mealType)) }
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun MessTimetableView(
    allMenu: List<MenuEntity>,
    onEdit: (Pair<List<MenuEntity>, Pair<DayOfWeek, MealType>>) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    
    val columnWidth = 140.dp
    val rowHeight = 120.dp // Increased height to accommodate multiple dishes
    val labelWidth = 100.dp

    Column(modifier = Modifier.fillMaxSize()) {
        // Day Headers
        Row(
            modifier = Modifier
                .padding(start = labelWidth)
                .horizontalScroll(horizontalScrollState)
        ) {
            DayOfWeek.entries.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(columnWidth)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            // Rows for Meal Types
            Column {
                MealType.entries.forEach { mealType ->
                    Row(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        // Sticky Label
                        Box(
                            modifier = Modifier
                                .width(labelWidth)
                                .height(rowHeight)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = mealType.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        // Meal Cells
                        DayOfWeek.entries.forEach { day ->
                            val menus = allMenu.filter { it.dayOfWeek == day && it.mealType == mealType }
                            Box(
                                modifier = Modifier
                                    .width(columnWidth)
                                    .height(rowHeight)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    .padding(4.dp)
                            ) {
                                MealGridCell(
                                    mealType = mealType,
                                    menus = menus,
                                    onClick = { onEdit(menus to (day to mealType)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MealGridCell(
    mealType: MealType,
    menus: List<MenuEntity>,
    onClick: () -> Unit
) {
    val backgroundColor = when (mealType) {
        MealType.BREAKFAST -> Color(0xFFFFF6D1) // Soft Yellow
        MealType.LUNCH -> Color(0xFFD1E9FF)     // Soft Blue
        MealType.DINNER -> Color(0xFFE9D1FF)    // Soft Purple
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (menus.isNotEmpty()) backgroundColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (menus.isNotEmpty()) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (menus.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    menus.forEach { menu ->
                        Text(
                            text = if (menus.size > 1) "• ${menu.itemsText}" else menu.itemsText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 12.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
