package com.example.workouttracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["timestamp"])
    ]
)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val fieldValues: List<Float>,
    val kpi: Float
) {
    companion object {
        /**
         * Calculates KPI = product of all field values.
         * If field count is 1, KPI = Field1.
         */
        fun calculateKpi(values: List<Float>): Float {
            if (values.isEmpty()) return 0f
            return values.fold(1f) { acc, v -> acc * v }
        }
    }
}
