package com.example.workouttracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.workouttracker.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun getLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE exerciseId = :exerciseId ORDER BY timestamp ASC")
    fun getLogsForExerciseChronological(exerciseId: Long): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE id = :id")
    suspend fun getLogById(id: Long): WorkoutLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLog): Long

    @Update
    suspend fun updateLog(log: WorkoutLog)

    @Delete
    suspend fun deleteLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM workout_logs WHERE exerciseId = :exerciseId")
    suspend fun deleteLogsForExercise(exerciseId: Long)
}
