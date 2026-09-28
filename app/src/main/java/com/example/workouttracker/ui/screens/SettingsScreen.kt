package com.example.workouttracker.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.components.GlassCard
import com.example.workouttracker.ui.theme.FrostCyanColors
import com.example.workouttracker.ui.theme.IronForgeThemeMode
import com.example.workouttracker.ui.theme.LocalGlassColors
import com.example.workouttracker.ui.theme.MidnightObsidianColors
import com.example.workouttracker.ui.theme.WarmRoseColors
import com.example.workouttracker.ui.theme.CyberTealColors
import com.example.workouttracker.ui.theme.getGlassColors

@Composable
fun SettingsScreen(
    currentTheme: IronForgeThemeMode,
    onThemeSelected: (IronForgeThemeMode) -> Unit,
    onSeedStarterClick: () -> Unit,
    onClearAllDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "App Settings & Themes",
                    color = glassColors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Theme Selection Section (4 Glassmorphism themes)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = glassColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Glassmorphism Themes",
                                color = glassColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        IronForgeThemeMode.values().forEach { mode ->
                            val isSelected = mode == currentTheme
                            val modeColors = getGlassColors(mode)

                            ThemeOptionRow(
                                mode = mode,
                                isSelected = isSelected,
                                backgroundBrush = Brush.horizontalGradient(
                                    listOf(modeColors.backgroundStart, modeColors.backgroundEnd)
                                ),
                                primaryColor = modeColors.primary,
                                isDark = modeColors.isDark,
                                onClick = { onThemeSelected(mode) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // KPI Algorithm Specification Info Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = glassColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IronForge KPI Calculation Engine",
                                color = glassColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Set Level Performance KPI:",
                            color = glassColors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "KPI = Field 1 × Field 2 × ... × Field N\n(If 1 field, KPI = Field 1). Example: 100 kg × 8 reps = 800.0 KPI.",
                            color = glassColors.textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Daily Selection & Tie-Breaking Rule:",
                            color = glassColors.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "When multiple sets are logged on the same calendar day:\n" +
                                    "1. The set with highest KPI is selected for the daily peak graph node.\n" +
                                    "2. Tie-Breaker: If KPIs are identical (e.g. 10kg × 12 reps = 120 vs. 15kg × 8 reps = 120), " +
                                    "the set with higher Field 1 (15kg > 10kg) wins.\n" +
                                    "3. If Field 1 is also equal, Field 2 is compared, and so on.\n" +
                                    "4. If all fields are equal, the latest timestamp is selected.",
                            color = glassColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Data Management Section
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Data Management",
                            color = glassColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Seed sample data button
                        Button(
                            onClick = onSeedStarterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seed_data_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = glassColors.primary.copy(alpha = 0.2f),
                                contentColor = glassColors.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Populate Sample Exercises & Multi-Month Logs", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Clear all data button
                        Button(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_data_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
                                contentColor = Color(0xFFFF5252)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // About section
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = glassColors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "IronForge v1.0",
                                color = glassColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Offline-first Room database, Jetpack Compose, dynamic time-frame resolution.",
                                color = glassColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Clear Confirmation Dialog
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                containerColor = glassColors.backgroundStart,
                title = {
                    Text(
                        "Reset All Data?",
                        color = glassColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "This will delete all exercises and all workout log history from your device. This action cannot be undone.",
                        color = glassColors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearConfirmDialog = false
                            onClearAllDataClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Reset Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel", color = glassColors.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: IronForgeThemeMode,
    isSelected: Boolean,
    backgroundBrush: Brush,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val border = if (isSelected) {
        BorderStroke(2.dp, glassColors.primary)
    } else {
        BorderStroke(0.8.dp, glassColors.borderGlass.copy(alpha = 0.3f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .border(border, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = primaryColor),
                onClick = onClick
            )
            .padding(14.dp)
            .testTag("theme_option_${mode.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color swatch
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = mode.displayName,
                        color = if (isDark) Color.White else Color(0xFF1A1A1A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDark) "Dark Frosted Glass" else "Light Frosted Glass",
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF333333),
                        fontSize = 11.sp
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
