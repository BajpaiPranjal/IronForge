package com.example.workouttracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.workouttracker.ui.theme.LocalGlassColors
import java.util.Locale

@Composable
fun HomeScreen(
    exercises: List<Exercise>,
    allLogs: List<WorkoutLog>,
    onSelectExercise: (Long) -> Unit,
    onCreateExerciseClick: () -> Unit,
    onLogSet: (Long, List<Float>) -> Unit,
    onSeedStarterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    var searchQuery by remember { mutableStateOf("") }
    var activeLogExercise by remember { mutableStateOf<Exercise?>(null) }

    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalSets = allLogs.size
    val peakKpi = allLogs.maxOfOrNull { it.kpi } ?: 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_banner_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(glassColors.primary.copy(alpha = 0.25f))
                                    .border(1.dp, glassColors.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "IronForge",
                                    tint = glassColors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "IRONFORGE",
                                    color = glassColors.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.8.sp
                                )
                                Text(
                                    text = "Workout Performance Tracker",
                                    color = glassColors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick KPI metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBox(
                                title = "Exercises",
                                value = exercises.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Sets Logged",
                                value = totalSets.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Top KPI",
                                value = if (peakKpi > 0) String.format(Locale.US, "%.1f", peakKpi) else "--",
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search exercises...",
                            color = glassColors.textSecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = glassColors.textSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = glassColors.surfaceGlass,
                        unfocusedContainerColor = glassColors.surfaceGlassVariant,
                        focusedBorderColor = glassColors.primary,
                        unfocusedBorderColor = glassColors.borderGlass.copy(alpha = 0.3f),
                        focusedTextColor = glassColors.textPrimary,
                        unfocusedTextColor = glassColors.textPrimary,
                        cursorColor = glassColors.primary
                    ),
                    singleLine = true
                )
            }

            // Exercise List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Exercises (${filteredExercises.size})",
                        color = glassColors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (exercises.isEmpty()) {
                        TextButton(
                            onClick = onSeedStarterClick,
                            modifier = Modifier.testTag("seed_starter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = glassColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Load Starters",
                                color = glassColors.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (filteredExercises.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = glassColors.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No exercises match \"$searchQuery\"" else "No exercises added yet",
                                color = glassColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create a custom exercise with up to 4 metrics or load starter templates to start tracking!",
                                color = glassColors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onSeedStarterClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = glassColors.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Load Starter Presets", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredExercises, key = { it.id }) { exercise ->
                    val logs = allLogs.filter { it.exerciseId == exercise.id }
                    val topKpi = logs.maxOfOrNull { it.kpi } ?: 0f

                    ExerciseItemCard(
                        exercise = exercise,
                        logCount = logs.size,
                        topKpi = topKpi,
                        onCardClick = { onSelectExercise(exercise.id) },
                        onQuickLogClick = { activeLogExercise = exercise }
                    )
                }
            }
        }

        // Floating Action Button for Create Exercise
        FloatingActionButton(
            onClick = onCreateExerciseClick,
            containerColor = glassColors.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_exercise_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Exercise",
                modifier = Modifier.size(28.dp)
            )
        }

        // Quick Log Set Dialog
        activeLogExercise?.let { exercise ->
            QuickLogDialog(
                exercise = exercise,
                onDismiss = { activeLogExercise = null },
                onConfirm = { values ->
                    onLogSet(exercise.id, values)
                    activeLogExercise = null
                }
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(glassColors.surfaceGlassVariant)
            .border(
                1.dp,
                glassColors.borderGlass.copy(alpha = 0.25f),
                RoundedCornerShape(14.dp)
            )
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = glassColors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = glassColors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ExerciseItemCard(
    exercise: Exercise,
    logCount: Int,
    topKpi: Float,
    onCardClick: () -> Unit,
    onQuickLogClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_card_${exercise.id}"),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        color = glassColors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GlassBadge(
                        text = exercise.formulaDisplay,
                        backgroundColor = glassColors.primary.copy(alpha = 0.15f),
                        textColor = glassColors.primary
                    )
                }

                // Quick Log Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(glassColors.primary.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            glassColors.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = glassColors.primary),
                            onClick = onQuickLogClick
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("quick_log_btn_${exercise.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = glassColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Log Set",
                            color = glassColors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = glassColors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$logCount sets logged",
                        color = glassColors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                if (topKpi > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = glassColors.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Peak KPI: ${String.format(Locale.US, "%.1f", topKpi)}",
                            color = glassColors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickLogDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onConfirm: (List<Float>) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val fieldValues = remember {
        mutableStateListOf<String>().apply {
            repeat(exercise.fieldLabels.size) { add("") }
        }
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = glassColors.backgroundStart,
        title = {
            Text(
                text = "Log Set: ${exercise.name}",
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
                    text = "Formula: ${exercise.formulaDisplay}",
                    color = glassColors.textSecondary,
                    fontSize = 12.sp
                )

                exercise.fieldLabels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = fieldValues[index],
                        onValueChange = { input ->
                            // Allow numbers and decimal point
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                                fieldValues[index] = input
                            }
                        },
                        label = { Text(label, color = glassColors.textSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_field_input_$index"),
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
                    Text(
                        text = msg,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp
                    )
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
                        onConfirm(parsed.filterNotNull())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = glassColors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_log_button")
            ) {
                Text("Save Set", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = glassColors.textSecondary)
            }
        }
    )
}
