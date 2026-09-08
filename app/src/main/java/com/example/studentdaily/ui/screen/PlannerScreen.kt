package com.example.studentdaily.ui.screen

import androidx.compose.foundation.BorderStroke
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
import com.example.studentdaily.data.model.ClassEntity
import com.example.studentdaily.data.model.DayOfWeek
import com.example.studentdaily.ui.components.ClassAttendanceItem
import com.example.studentdaily.ui.components.EditClassDialog
import com.example.studentdaily.ui.viewmodel.AttendanceStats
import com.example.studentdaily.ui.viewmodel.AttendanceViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

enum class PlannerMode {
    Timetable, Weekly, Daily
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: AttendanceViewModel,
    onAddClass: () -> Unit
) {
    val classes by viewModel.allClasses.collectAsState(initial = null)
    val allStats by viewModel.allAttendanceStats.collectAsState(initial = emptyMap())
    
    var mode by remember { mutableStateOf(PlannerMode.Weekly) }
    var editingClass by remember { mutableStateOf<ClassEntity?>(null) }
    
    // For Daily mode
    var selectedDay by remember { 
        val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase()
        mutableStateOf(try { DayOfWeek.valueOf(today) } catch (_: Exception) { DayOfWeek.MON })
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Planner") },
                    actions = {
                        IconButton(onClick = onAddClass) {
                            Icon(Icons.Default.Add, contentDescription = "Add Class")
                        }
                    },
                    windowInsets = TopAppBarDefaults.windowInsets
                )
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    PlannerMode.entries.forEachIndexed { index, plannerMode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = PlannerMode.entries.size),
                            onClick = { mode = plannerMode },
                            selected = mode == plannerMode
                        ) {
                            Text(plannerMode.name)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (mode) {
                PlannerMode.Timetable -> {
                    TimetableView(
                        classes = classes ?: emptyList(),
                        onEdit = { editingClass = it }
                    )
                }
                PlannerMode.Weekly -> {
                    WeeklyListView(
                        classes = classes,
                        allStats = allStats,
                        onAddClass = onAddClass,
                        onEdit = { editingClass = it }
                    )
                }
                PlannerMode.Daily -> {
                    DailyView(
                        classes = classes ?: emptyList(),
                        selectedDay = selectedDay,
                        allStats = allStats,
                        onDaySelected = { selectedDay = it },
                        onAddClass = onAddClass,
                        onEdit = { editingClass = it }
                    )
                }
            }
        }
    }

    editingClass?.let { entity ->
        EditClassDialog(
            classEntity = entity,
            onDismiss = { editingClass = null },
            onUpdate = { viewModel.updateClass(it) },
            onDelete = { viewModel.deleteClass(it) }
        )
    }
}

@Composable
fun WeeklyListView(
    classes: List<ClassEntity>?,
    allStats: Map<Long, AttendanceStats>,
    onAddClass: () -> Unit,
    onEdit: (ClassEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "weekly_header") {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Weekly Schedule & Attendance", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (classes == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (classes.isEmpty()) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("No classes scheduled.", color = MaterialTheme.colorScheme.outline)
                    Button(onClick = onAddClass) {
                        Text("Add your first class")
                    }
                }
            }
        } else {
            DayOfWeek.entries.forEach { day ->
                val classesForDay = classes.filter { it.dayOfWeek == day }
                if (classesForDay.isNotEmpty()) {
                    item {
                        Text(day.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(classesForDay, key = { "class_${it.id}" }) { classEntity ->
                        ClassAttendanceItem(
                            classEntity = classEntity,
                            stats = allStats[classEntity.id],
                            currentStatus = null,
                            onMarkAttendance = {},
                            onEdit = { onEdit(classEntity) }
                        )
                    }
                }
            }
        }
        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyView(
    classes: List<ClassEntity>,
    selectedDay: DayOfWeek,
    allStats: Map<Long, AttendanceStats>,
    onDaySelected: (DayOfWeek) -> Unit,
    onAddClass: () -> Unit,
    onEdit: (ClassEntity) -> Unit
) {
    val classesForDay = remember(classes, selectedDay) {
        classes.filter { it.dayOfWeek == selectedDay }
            .sortedBy { it.startTime }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedDay.ordinal,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            DayOfWeek.entries.forEach { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { onDaySelected(day) },
                    text = { Text(day.name) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            if (classesForDay.isEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("No classes scheduled for ${selectedDay.name}.", color = MaterialTheme.colorScheme.outline)
                        TextButton(onClick = onAddClass) {
                            Text("+ Add a class")
                        }
                    }
                }
            } else {
                items(classesForDay, key = { "daily_class_${it.id}" }) { classEntity ->
                    ClassAttendanceItem(
                        classEntity = classEntity,
                        stats = allStats[classEntity.id],
                        currentStatus = null,
                        onMarkAttendance = {},
                        onEdit = { onEdit(classEntity) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TimetableView(
    classes: List<ClassEntity>,
    onEdit: (ClassEntity) -> Unit
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // Determine the time range (start and end hour)
    val startHour = remember(classes) {
        classes.minOfOrNull { 
            try { LocalTime.parse(it.startTime, timeFormatter).hour } catch(_: Exception) { 9 }
        }?.coerceAtMost(8) ?: 8
    }
    val endHour = remember(classes) {
        classes.maxOfOrNull { 
            try { LocalTime.parse(it.endTime, timeFormatter).hour } catch(_: Exception) { 18 }
        }?.coerceAtLeast(18) ?: 18
    }

    val hours = (startHour..endHour).toList()
    val columnWidth = 140.dp // Increased for better overlap visibility
    val hourHeight = 90.dp  // Increased for better readability
    val timeLabelWidth = 56.dp

    // Layout calculation for overlapping classes
    val dayLayouts = remember(classes) {
        DayOfWeek.entries.associateWith { day ->
            calculateDayLayout(classes.filter { it.dayOfWeek == day })
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Headers (Days)
        Row(
            modifier = Modifier
                .padding(start = timeLabelWidth)
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
            // Grid and Blocks
            Row(
                modifier = Modifier
                    .padding(start = timeLabelWidth)
                    .horizontalScroll(horizontalScrollState)
            ) {
                DayOfWeek.entries.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(columnWidth)
                            .height(hourHeight * hours.size)
                    ) {
                        // Background slot lines
                        Column {
                            hours.forEach { _ ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(hourHeight)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                )
                            }
                        }

                        // Day separator line (vertical)
                        Box(modifier = Modifier.fillMaxHeight().width(0.5.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))

                        // Class Blocks
                        dayLayouts[day]?.forEach { layoutInfo ->
                            val classEntity = layoutInfo.classEntity
                            val startTime = try { LocalTime.parse(classEntity.startTime, timeFormatter) } catch(_: Exception) { null }
                            val endTime = try { LocalTime.parse(classEntity.endTime, timeFormatter) } catch(_: Exception) { null }
                            
                            if (startTime != null && endTime != null) {
                                val startOffsetMinutes = (startTime.hour - startHour) * 60 + startTime.minute
                                val durationMinutes = (endTime.hour - startTime.hour) * 60 + (endTime.minute - startTime.minute)
                                
                                val topOffset = (startOffsetMinutes.toFloat() / 60f) * hourHeight.value
                                val blockHeight = (durationMinutes.toFloat() / 60f) * hourHeight.value
                                
                                val blockWidth = columnWidth / layoutInfo.totalLanes
                                val startPadding = blockWidth * layoutInfo.laneIndex

                                TimetableBlock(
                                    classEntity = classEntity,
                                    modifier = Modifier
                                        .offset(x = startPadding, y = topOffset.dp)
                                        .width(blockWidth)
                                        .height(blockHeight.dp)
                                        .padding(1.dp),
                                    onClick = { onEdit(classEntity) }
                                )
                            }
                        }
                    }
                }
            }

            // Time Labels (sticky on the left, but scrolls vertically)
            Column(
                modifier = Modifier
                    .width(timeLabelWidth)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                hours.forEach { hour ->
                    Box(
                        modifier = Modifier
                            .height(hourHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:00", hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableBlock(
    classEntity: ClassEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = getSubjectColor(classEntity.subjectName)
    val contentColor = if (isDarkColor(backgroundColor)) Color.White else Color.Black

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        tonalElevation = 4.dp,
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = classEntity.subjectName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
            
            Text(
                text = "${classEntity.startTime} - ${classEntity.endTime}",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

data class ClassLayoutInfo(
    val classEntity: ClassEntity,
    val laneIndex: Int,
    val totalLanes: Int
)

fun calculateDayLayout(dayClasses: List<ClassEntity>): List<ClassLayoutInfo> {
    if (dayClasses.isEmpty()) return emptyList()

    // 1. Sort by start time
    val sorted = dayClasses.sortedBy { it.startTime }
    
    // 2. Group into overlapping clusters
    val clusters = mutableListOf<MutableList<ClassEntity>>()
    for (classEntity in sorted) {
        val lastCluster = clusters.lastOrNull()
        if (lastCluster == null) {
            clusters.add(mutableListOf(classEntity))
        } else {
            // Check if this class overlaps with ANY class in the current cluster
            // A simple check is to compare its start time with the maximum end time in the cluster
            val maxEndTimeStr = lastCluster.maxOf { it.endTime }
            if (classEntity.startTime < maxEndTimeStr) {
                lastCluster.add(classEntity)
            } else {
                clusters.add(mutableListOf(classEntity))
            }
        }
    }

    // 3. Position classes within each cluster using lane assignment
    val result = mutableListOf<ClassLayoutInfo>()
    for (cluster in clusters) {
        val lanes = mutableListOf<MutableList<ClassEntity>>()
        
        for (classEntity in cluster) {
            var assigned = false
            for (i in lanes.indices) {
                val lane = lanes[i]
                // Check if classEntity overlaps with the last class in this lane
                val lastInLane = lane.last()
                if (classEntity.startTime >= lastInLane.endTime) {
                    lane.add(classEntity)
                    assigned = true
                    break
                }
            }
            if (!assigned) {
                lanes.add(mutableListOf(classEntity))
            }
        }
        
        val totalLanes = lanes.size
        for (i in lanes.indices) {
            for (classEntity in lanes[i]) {
                result.add(ClassLayoutInfo(classEntity, i, totalLanes))
            }
        }
    }
    
    return result
}

fun getSubjectColor(subject: String): Color {
    val colors = listOf(
        Color(0xFFFFD1D1), // Soft Red
        Color(0xFFD1E9FF), // Soft Blue
        Color(0xFFD1FFD1), // Soft Green
        Color(0xFFFFF6D1), // Soft Yellow
        Color(0xFFE9D1FF), // Soft Purple
        Color(0xFFD1FFF6), // Soft Cyan
        Color(0xFFFFE0D1), // Soft Orange
        Color(0xFFF0F0F0), // Soft Gray
        Color(0xFFE0FFD1), // Soft Lime
        Color(0xFFFFD1F6)  // Soft Pink
    )
    
    val hash = subject.lowercase().trim().hashCode()
    return colors[abs(hash) % colors.size]
}

fun isDarkColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
