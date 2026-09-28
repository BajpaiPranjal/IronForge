package com.example.workouttracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.WorkoutLog
import com.example.workouttracker.ui.components.GlassBadge
import com.example.workouttracker.ui.components.GlassCard
import com.example.workouttracker.ui.components.PerformanceGraph
import com.example.workouttracker.ui.components.TimeRange
import com.example.workouttracker.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseDetailScreen(
    exercise: Exercise,
    logs: List<WorkoutLog>,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    onBackClick: () -> Unit,
    onLogSet: (Long, List<Float>, Long) -> Unit,
    onUpdateLog: (Long, Long, List<Float>, Long) -> Unit,
    onDeleteLog: (WorkoutLog) -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    BackHandler { onBackClick() }

    var isAddingLog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WorkoutLog?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Statistics calculations
    val totalSets = logs.size
    val peakKpi = logs.maxOfOrNull { it.kpi } ?: 0f
    val avgKpi = if (logs.isNotEmpty()) logs.map { it.kpi }.average().toFloat() else 0f
    val latestLog = logs.maxByOrNull { it.timestamp }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("exercise_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top App Bar row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = glassColors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = exercise.name,
                                color = glassColors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Formula: ${exercise.formulaDisplay}",
                                color = glassColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // More options menu (Delete exercise)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("exercise_options_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = glassColors.textPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Exercise",
                                        color = Color(0xFFFF5252),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteConfirmDialog = true
                                },
                                modifier = Modifier.testTag("delete_exercise_menu_item")
                            )
                        }
                    }
                }
            }

            // Trend Graph Section
            item {
                PerformanceGraph(
                    exercise = exercise,
                    logs = logs,
                    selectedRange = selectedRange,
                    onRangeSelected = onRangeSelected
                )
            }

            // Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Peak KPI",
                        value = if (peakKpi > 0) String.format(Locale.US, "%.1f", peakKpi) else "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avg KPI",
                        value = if (avgKpi > 0) String.format(Locale.US, "%.1f", avgKpi) else "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Sets",
                        value = totalSets.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Log History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "History Logs (${logs.size})",
                        color = glassColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { isAddingLog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = glassColors.primary.copy(alpha = 0.25f),
                            contentColor = glassColors.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("detail_log_set_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = glassColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Set", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // History Log Items
            if (logs.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No sets recorded yet",
                                color = glassColors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Log Set' above to record your first set",
                                color = glassColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    WorkoutLogItemCard(
                        exercise = exercise,
                        log = log,
                        onEditClick = { editingLog = log },
                        onDeleteClick = { onDeleteLog(log) }
                    )
                }
            }
        }

        // Add Log Dialog
        if (isAddingLog) {
            EditOrAddLogDialog(
                title = "Log New Set",
                exercise = exercise,
                initialValues = List(exercise.fieldLabels.size) { "" },
                initialTimestamp = System.currentTimeMillis(),
                onDismiss = { isAddingLog = false },
                onConfirm = { values, timestamp ->
                    onLogSet(exercise.id, values, timestamp)
                    isAddingLog = false
                }
            )
        }

        // Edit Log Dialog
        editingLog?.let { log ->
            EditOrAddLogDialog(
                title = "Edit Set",
                exercise = exercise,
                initialValues = log.fieldValues.map {
                    if (it % 1f == 0f) it.toInt().toString() else it.toString()
                },
                initialTimestamp = log.timestamp,
                onDismiss = { editingLog = null },
                onConfirm = { values, timestamp ->
                    onUpdateLog(log.id, exercise.id, values, timestamp)
                    editingLog = null
                }
            )
        }

        // Delete Exercise Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = glassColors.backgroundStart,
                title = {
                    Text(
                        "Delete Exercise?",
                        color = glassColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to delete \"${exercise.name}\"? This will permanently delete all $totalSets logged sets associated with this exercise.",
                        color = glassColors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteExercise(exercise)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = glassColors.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = glassColors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = glassColors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun WorkoutLogItemCard(
    exercise: Exercise,
    log: WorkoutLog,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    color = glassColors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Field Values display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    exercise.fieldLabels.forEachIndexed { i, label ->
                        val value = log.fieldValues.getOrNull(i) ?: 0f
                        val formattedVal = if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
                        Text(
                            text = "$label: $formattedVal",
                            color = glassColors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (i < exercise.fieldLabels.lastIndex) {
                            Text("•", color = glassColors.primary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // KPI Badge
                GlassBadge(
                    text = "KPI: ${String.format(Locale.US, "%.1f", log.kpi)}",
                    backgroundColor = glassColors.primary.copy(alpha = 0.2f),
                    textColor = glassColors.primary
                )
            }

            // Edit & Delete icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_log_${log.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit set",
                        tint = glassColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_log_${log.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete set",
                        tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditOrAddLogDialog(
    title: String,
    exercise: Exercise,
    initialValues: List<String>,
    initialTimestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (List<Float>, Long) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val fieldValues = remember {
        mutableStateListOf<String>().apply {
            addAll(initialValues)
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = glassColors.backgroundStart,
        title = {
            Text(
                text = title,
                color = glassColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Exercise: ${exercise.name} (${exercise.formulaDisplay})",
                    color = glassColors.textSecondary,
                    fontSize = 12.sp
                )

                exercise.fieldLabels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = fieldValues.getOrElse(index) { "" },
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                if (index < fieldValues.size) {
                                    fieldValues[index] = input
                                }
                            }
                        },
                        label = { Text(label, color = glassColors.textSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_log_input_$index"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = glassColors.textPrimary,
                            unfocusedTextColor = glassColors.textPrimary,
                            focusedBorderColor = glassColors.primary,
                            unfocusedBorderColor = glassColors.borderGlass.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }

                // Calculated KPI preview
                val calculatedKpi = remember(fieldValues.toList()) {
                    val nums = fieldValues.mapNotNull { it.toFloatOrNull() }
                    if (nums.size == exercise.fieldLabels.size && nums.isNotEmpty()) {
                        nums.fold(1f) { acc, f -> acc * f }
                    } else null
                }

                calculatedKpi?.let { kpi ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassColors.primary.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Resulting KPI = ${String.format(Locale.US, "%.1f", kpi)}",
                            color = glassColors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                errorMessage?.let { msg ->
                    Text(text = msg, color = Color(0xFFFF5252), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = fieldValues.map { it.toFloatOrNull() }
                    if (parsed.any { it == null || it <= 0f }) {
                        errorMessage = "Please enter valid positive numbers for all fields"
                    } else {
                        onConfirm(parsed.filterNotNull(), initialTimestamp)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = glassColors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_log_confirm_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = glassColors.textSecondary)
            }
        }
    )
}
