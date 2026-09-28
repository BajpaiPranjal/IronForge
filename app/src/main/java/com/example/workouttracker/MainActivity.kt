package com.example.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.workouttracker.data.local.AppDatabase
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.ui.screens.CreateExerciseScreen
import com.example.workouttracker.ui.screens.ExerciseDetailScreen
import com.example.workouttracker.ui.screens.HomeScreen
import com.example.workouttracker.ui.screens.SettingsScreen
import com.example.workouttracker.ui.theme.IronForgeTheme
import com.example.workouttracker.ui.theme.LocalGlassColors
import com.example.workouttracker.ui.viewmodel.WorkoutViewModel
import com.example.workouttracker.ui.viewmodel.WorkoutViewModelFactory

sealed class Screen {
    data object Home : Screen()
    data class Detail(val exerciseId: Long) : Screen()
    data object Create : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = WorkoutRepository(database.exerciseDao(), database.workoutLogDao())
        WorkoutViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val exercises by viewModel.allExercises.collectAsStateWithLifecycle()
            val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
            val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            IronForgeTheme(themeMode = themeMode) {
                val glassColors = LocalGlassColors.current

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(glassColors.backgroundBrush),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        // Frosted Glass Bottom Navigation Bar
                        GlassBottomNavigationBar(
                            currentScreen = currentScreen,
                            onNavigate = { screen -> currentScreen = screen }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    exercises = exercises,
                                    allLogs = allLogs,
                                    onSelectExercise = { id -> currentScreen = Screen.Detail(id) },
                                    onCreateExerciseClick = { currentScreen = Screen.Create },
                                    onLogSet = { exerciseId, values ->
                                        viewModel.logSet(exerciseId, values)
                                    },
                                    onSeedStarterClick = {
                                        viewModel.seedStarterData()
                                    }
                                )
                            }

                            is Screen.Detail -> {
                                val exercise = exercises.firstOrNull { it.id == screen.exerciseId }
                                if (exercise != null) {
                                    val exerciseLogs = allLogs.filter { it.exerciseId == exercise.id }
                                    ExerciseDetailScreen(
                                        exercise = exercise,
                                        logs = exerciseLogs,
                                        selectedRange = selectedTimeRange,
                                        onRangeSelected = { viewModel.setTimeRange(it) },
                                        onBackClick = { currentScreen = Screen.Home },
                                        onLogSet = { exId, values, ts ->
                                            viewModel.logSet(exId, values, ts)
                                        },
                                        onUpdateLog = { logId, exId, values, ts ->
                                            viewModel.updateLog(logId, exId, values, ts)
                                        },
                                        onDeleteLog = { log ->
                                            viewModel.deleteLog(log)
                                        },
                                        onDeleteExercise = { ex ->
                                            viewModel.deleteExercise(ex)
                                            currentScreen = Screen.Home
                                        }
                                    )
                                } else {
                                    // Fallback if exercise was deleted
                                    currentScreen = Screen.Home
                                }
                            }

                            is Screen.Create -> {
                                CreateExerciseScreen(
                                    onBackClick = { currentScreen = Screen.Home },
                                    onExerciseCreated = { name, count, labels ->
                                        viewModel.createExercise(name, count, labels) { newId ->
                                            currentScreen = Screen.Detail(newId)
                                        }
                                    }
                                )
                            }

                            is Screen.Settings -> {
                                SettingsScreen(
                                    currentTheme = themeMode,
                                    onThemeSelected = { viewModel.setThemeMode(it) },
                                    onSeedStarterClick = { viewModel.seedStarterData() },
                                    onClearAllDataClick = { viewModel.clearAllData() }
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
fun GlassBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassColors.surfaceGlass,
                            glassColors.surfaceGlassVariant
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glassColors.borderGlass,
                            glassColors.borderGlass.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .testTag("glass_bottom_nav"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Exercises",
                    selectedIcon = Icons.Default.FitnessCenter,
                    unselectedIcon = Icons.Outlined.FitnessCenter,
                    isSelected = currentScreen is Screen.Home || currentScreen is Screen.Detail,
                    onClick = { onNavigate(Screen.Home) },
                    testTag = "nav_item_home"
                )

                BottomNavItem(
                    label = "Create",
                    selectedIcon = Icons.Default.AddCircle,
                    unselectedIcon = Icons.Outlined.AddCircleOutline,
                    isSelected = currentScreen is Screen.Create,
                    onClick = { onNavigate(Screen.Create) },
                    testTag = "nav_item_create"
                )

                BottomNavItem(
                    label = "Settings",
                    selectedIcon = Icons.Default.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    isSelected = currentScreen is Screen.Settings,
                    onClick = { onNavigate(Screen.Settings) },
                    testTag = "nav_item_settings"
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val glassColors = LocalGlassColors.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = glassColors.primary),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (isSelected) glassColors.primary else glassColors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) glassColors.primary else glassColors.textSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
