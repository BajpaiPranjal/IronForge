package com.example.workouttracker.data.repository

import com.example.workouttracker.data.local.ExerciseDao
import com.example.workouttracker.data.local.WorkoutLogDao
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutLogDao: WorkoutLogDao
) {
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    val allLogs: Flow<List<WorkoutLog>> = workoutLogDao.getAllLogs()

    fun getExercise(id: Long): Flow<Exercise?> = exerciseDao.getExerciseById(id)

    suspend fun getExerciseOnce(id: Long): Exercise? = exerciseDao.getExerciseByIdOnce(id)

    fun getLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>> =
        workoutLogDao.getLogsForExercise(exerciseId)

    fun getLogsForExerciseChronological(exerciseId: Long): Flow<List<WorkoutLog>> =
        workoutLogDao.getLogsForExerciseChronological(exerciseId)

    suspend fun insertExercise(exercise: Exercise): Long =
        exerciseDao.insertExercise(exercise)

    suspend fun updateExercise(exercise: Exercise) =
        exerciseDao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: Exercise) =
        exerciseDao.deleteExercise(exercise)

    suspend fun deleteExerciseById(id: Long) =
        exerciseDao.deleteExerciseById(id)

    suspend fun insertLog(log: WorkoutLog): Long =
        workoutLogDao.insertLog(log)

    suspend fun updateLog(log: WorkoutLog) =
        workoutLogDao.updateLog(log)

    suspend fun deleteLog(log: WorkoutLog) =
        workoutLogDao.deleteLog(log)

    suspend fun deleteLogById(id: Long) =
        workoutLogDao.deleteLogById(id)
}
