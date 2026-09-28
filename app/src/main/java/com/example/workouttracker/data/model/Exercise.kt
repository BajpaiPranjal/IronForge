package com.example.workouttracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val fieldCount: Int,
    val fieldLabels: List<String>,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formulaDisplay: String
        get() = if (fieldLabels.isEmpty()) "KPI" else fieldLabels.joinToString(" × ")
}
