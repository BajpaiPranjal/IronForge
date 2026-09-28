package com.example.workouttracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.WorkoutLog
import com.example.workouttracker.data.repository.WorkoutRepository
import com.example.workouttracker.ui.components.TimeRange
import com.example.workouttracker.ui.theme.IronForgeThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkoutViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<WorkoutLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _themeMode = MutableStateFlow(IronForgeThemeMode.MIDNIGHT_OBSIDIAN)
    val themeMode: StateFlow<IronForgeThemeMode> = _themeMode.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.CURRENT_WEEK)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    init {
        // Auto-seed starter exercises and sample data if empty
        viewModelScope.launch {
            repository.allExercises.collect { exercises ->
                if (exercises.isEmpty()) {
                    seedStarterData()
                }
            }
        }
    }

    fun setThemeMode(mode: IronForgeThemeMode) {
        _themeMode.value = mode
    }

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun createExercise(name: String, fieldCount: Int, fieldLabels: List<String>, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val exercise = Exercise(
                name = name.trim(),
                fieldCount = fieldCount,
                fieldLabels = fieldLabels.map { it.trim() }
            )
            val id = repository.insertExercise(exercise)
            onCreated(id)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun logSet(exerciseId: Long, fieldValues: List<Float>, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val kpi = WorkoutLog.calculateKpi(fieldValues)
            val log = WorkoutLog(
                exerciseId = exerciseId,
                timestamp = timestamp,
                fieldValues = fieldValues,
                kpi = kpi
            )
            repository.insertLog(log)
        }
    }

    fun updateLog(logId: Long, exerciseId: Long, fieldValues: List<Float>, timestamp: Long) {
        viewModelScope.launch {
            val kpi = WorkoutLog.calculateKpi(fieldValues)
            val log = WorkoutLog(
                id = logId,
                exerciseId = exerciseId,
                timestamp = timestamp,
                fieldValues = fieldValues,
                kpi = kpi
            )
            repository.updateLog(log)
        }
    }

    fun deleteLog(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun seedStarterData() {
        viewModelScope.launch {
            // Preset 1: Bench Press (Weight × Reps)
            val benchId = repository.insertExercise(
                Exercise(
                    name = "Bench Press",
                    fieldCount = 2,
                    fieldLabels = listOf("Weight (kg)", "Reps")
                )
            )

            // Preset 2: Squat (Weight × Reps)
            val squatId = repository.insertExercise(
                Exercise(
                    name = "Barbell Squat",
                    fieldCount = 2,
                    fieldLabels = listOf("Weight (kg)", "Reps")
                )
            )

            // Preset 3: Pull-ups (Reps only)
            val pullupId = repository.insertExercise(
                Exercise(
                    name = "Pull-ups",
                    fieldCount = 1,
                    fieldLabels = listOf("Reps")
                )
            )

            // Preset 4: Running (Speed × Duration)
            val runningId = repository.insertExercise(
                Exercise(
                    name = "Treadmill Run",
                    fieldCount = 2,
                    fieldLabels = listOf("Speed (km/h)", "Duration (mins)")
                )
            )

            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            // Multi-month sample sets for Bench Press to showcase all graph ranges
            // Past week sets:
            // Day 0 (today)
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 2 * 3600 * 1000, fieldValues = listOf(80f, 10f), kpi = 800f))
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now, fieldValues = listOf(85f, 10f), kpi = 850f)) // tie-breaker higher field 1

            // 2 days ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 2 * dayMillis, fieldValues = listOf(80f, 8f), kpi = 640f))
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 2 * dayMillis + 3600 * 1000, fieldValues = listOf(82.5f, 8f), kpi = 660f))

            // 4 days ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 4 * dayMillis, fieldValues = listOf(75f, 10f), kpi = 750f))

            // 2 weeks ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 14 * dayMillis, fieldValues = listOf(75f, 8f), kpi = 600f))

            // 4 weeks ago (~1 month)
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 28 * dayMillis, fieldValues = listOf(70f, 10f), kpi = 700f))

            // 6 weeks ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 42 * dayMillis, fieldValues = listOf(70f, 8f), kpi = 560f))

            // 2.5 months ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 75 * dayMillis, fieldValues = listOf(65f, 10f), kpi = 650f))

            // 4 months ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 120 * dayMillis, fieldValues = listOf(60f, 10f), kpi = 600f))

            // 5.5 months ago
            repository.insertLog(WorkoutLog(exerciseId = benchId, timestamp = now - 165 * dayMillis, fieldValues = listOf(55f, 10f), kpi = 550f))

            // Sample sets for Pull-ups (1 field)
            repository.insertLog(WorkoutLog(exerciseId = pullupId, timestamp = now, fieldValues = listOf(15f), kpi = 15f))
            repository.insertLog(WorkoutLog(exerciseId = pullupId, timestamp = now - 3 * dayMillis, fieldValues = listOf(14f), kpi = 14f))
            repository.insertLog(WorkoutLog(exerciseId = pullupId, timestamp = now - 10 * dayMillis, fieldValues = listOf(12f), kpi = 12f))
            repository.insertLog(WorkoutLog(exerciseId = pullupId, timestamp = now - 30 * dayMillis, fieldValues = listOf(10f), kpi = 10f))

            // Sample sets for Treadmill Run (Speed × Duration)
            repository.insertLog(WorkoutLog(exerciseId = runningId, timestamp = now - 1 * dayMillis, fieldValues = listOf(11.5f, 30f), kpi = 345f))
            repository.insertLog(WorkoutLog(exerciseId = runningId, timestamp = now - 5 * dayMillis, fieldValues = listOf(11.0f, 25f), kpi = 275f))
            repository.insertLog(WorkoutLog(exerciseId = runningId, timestamp = now - 15 * dayMillis, fieldValues = listOf(10.5f, 25f), kpi = 262.5f))

            // Sample sets for Squat
            repository.insertLog(WorkoutLog(exerciseId = squatId, timestamp = now - 1 * dayMillis, fieldValues = listOf(100f, 8f), kpi = 800f))
            repository.insertLog(WorkoutLog(exerciseId = squatId, timestamp = now - 8 * dayMillis, fieldValues = listOf(95f, 8f), kpi = 760f))
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            allExercises.value.forEach { exercise ->
                repository.deleteExercise(exercise)
            }
        }
    }
}

class WorkoutViewModelFactory(
    private val repository: WorkoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
