package com.example.workouttracker.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.components.GlassCard
import com.example.workouttracker.ui.theme.LocalGlassColors

enum class ExercisePreset(
    val title: String,
    val subtitle: String,
    val fieldCount: Int,
    val defaultLabels: List<String>,
    val icon: ImageVector
) {
    REPS_WEIGHT(
        title = "Reps × Weight",
        subtitle = "Strength exercises (e.g., Bench Press, Squat, Deadlift)",
        fieldCount = 2,
        defaultLabels = listOf("Weight (kg)", "Reps"),
        icon = Icons.Default.FitnessCenter
    ),
    REPS_ONLY(
        title = "Reps Only",
        subtitle = "Calisthenics (e.g., Push-ups, Pull-ups, Dips)",
        fieldCount = 1,
        defaultLabels = listOf("Reps"),
        icon = Icons.Default.FitnessCenter
    ),
    SPEED_DURATION(
        title = "Speed × Duration",
        subtitle = "Cardio exercises (e.g., Running, Cycling, Rowing)",
        fieldCount = 2,
        defaultLabels = listOf("Speed (km/h)", "Duration (mins)"),
        icon = Icons.Default.Speed
    ),
    CUSTOM(
        title = "Custom Setup",
        subtitle = "Configure 1 to 4 custom performance metrics",
        fieldCount = 2,
        defaultLabels = listOf("Metric 1", "Metric 2"),
        icon = Icons.Default.Tune
    )
}

@Composable
fun CreateExerciseScreen(
    onBackClick: () -> Unit,
    onExerciseCreated: (String, Int, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    BackHandler { onBackClick() }

    var exerciseName by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(ExercisePreset.REPS_WEIGHT) }
    var fieldCount by remember { mutableIntStateOf(2) }
    val fieldLabels = remember {
        mutableStateListOf("Weight (kg)", "Reps", "Metric 3", "Metric 4")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun applyPreset(preset: ExercisePreset) {
        selectedPreset = preset
        fieldCount = preset.fieldCount
        preset.defaultLabels.forEachIndexed { index, label ->
            if (index < fieldLabels.size) {
                fieldLabels[index] = label
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_exercise_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("create_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = glassColors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Exercise Wizard",
                        color = glassColors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Exercise Name input
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Exercise Name",
                            color = glassColors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = {
                                exerciseName = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text(
                                    "e.g., Incline Dumbbell Press",
                                    color = glassColors.textSecondary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exercise_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = glassColors.textPrimary,
                                unfocusedTextColor = glassColors.textPrimary,
                                focusedBorderColor = glassColors.primary,
                                unfocusedBorderColor = glassColors.borderGlass.copy(alpha = 0.5f),
                                focusedContainerColor = glassColors.surfaceGlass,
                                unfocusedContainerColor = glassColors.surfaceGlassVariant
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Presets selector
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Quick Presets / Suggestions",
                            color = glassColors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ExercisePreset.values().forEach { preset ->
                            val isSelected = preset == selectedPreset
                            PresetOptionCard(
                                preset = preset,
                                isSelected = isSelected,
                                onClick = { applyPreset(preset) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Field Configuration Section
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Performance Metric Fields",
                            color = glassColors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose number of fields (1 to 4) to multiply into your KPI score:",
                            color = glassColors.textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Field count selector (1, 2, 3, 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (1..4).forEach { count ->
                                val isSelected = count == fieldCount
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) glassColors.primary else glassColors.surfaceGlassVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) glassColors.primary else glassColors.borderGlass.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            fieldCount = count
                                            selectedPreset = ExercisePreset.CUSTOM
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("field_count_option_$count"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count ${if (count == 1) "Field" else "Fields"}",
                                        color = if (isSelected) Color.White else glassColors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom labels input for active field count
                        for (i in 0 until fieldCount) {
                            Text(
                                text = "Field ${i + 1} Label:",
                                color = glassColors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = fieldLabels.getOrElse(i) { "" },
                                onValueChange = { newVal ->
                                    if (i < fieldLabels.size) {
                                        fieldLabels[i] = newVal
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_field_label_$i"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = glassColors.textPrimary,
                                    unfocusedTextColor = glassColors.textPrimary,
                                    focusedBorderColor = glassColors.primary,
                                    unfocusedBorderColor = glassColors.borderGlass.copy(alpha = 0.5f),
                                    focusedContainerColor = glassColors.surfaceGlass,
                                    unfocusedContainerColor = glassColors.surfaceGlassVariant
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Formula Preview Box
                        val activeLabels = fieldLabels.take(fieldCount)
                        val formulaText = if (activeLabels.isEmpty()) "KPI" else activeLabels.joinToString(" × ")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(glassColors.primary.copy(alpha = 0.15f))
                                .border(1.dp, glassColors.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Calculated KPI Formula:",
                                    color = glassColors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "KPI = $formulaText",
                                    color = glassColors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Error message if any
            errorMessage?.let { err ->
                item {
                    Text(
                        text = err,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Save Exercise Button
            item {
                Button(
                    onClick = {
                        val name = exerciseName.trim()
                        if (name.isBlank()) {
                            errorMessage = "Please enter an exercise name"
                            return@Button
                        }
                        val labels = fieldLabels.take(fieldCount).map { it.trim() }
                        if (labels.any { it.isBlank() }) {
                            errorMessage = "All metric field labels must have names"
                            return@Button
                        }

                        onExerciseCreated(name, fieldCount, labels)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_exercise_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = glassColors.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Create Exercise",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetOptionCard(
    preset: ExercisePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val border = if (isSelected) {
        BorderStroke(1.5.dp, glassColors.primary)
    } else {
        BorderStroke(0.8.dp, glassColors.borderGlass.copy(alpha = 0.25f))
    }

    val background = if (isSelected) {
        glassColors.primary.copy(alpha = 0.12f)
    } else {
        glassColors.surfaceGlassVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(border, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = glassColors.primary),
                onClick = onClick
            )
            .padding(12.dp)
            .testTag("preset_${preset.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) glassColors.primary else glassColors.borderGlass.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else glassColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.title,
                    color = glassColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = preset.subtitle,
                    color = glassColors.textSecondary,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = glassColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
