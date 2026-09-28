package com.example

import com.example.workouttracker.data.model.WorkoutLog
import com.example.workouttracker.ui.components.GraphProcessor
import com.example.workouttracker.ui.components.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testKpiCalculation() {
        // Single field: Reps only
        assertEquals(15f, WorkoutLog.calculateKpi(listOf(15f)), 0.001f)

        // Two fields: Weight x Reps
        assertEquals(850f, WorkoutLog.calculateKpi(listOf(85f, 10f)), 0.001f)

        // Three fields: Speed x Duration x Incline
        assertEquals(600f, WorkoutLog.calculateKpi(listOf(10f, 30f, 2f)), 0.001f)
    }

    @Test
    fun testDailyTieBreakingRule() {
        val now = System.currentTimeMillis()

        // Entry A: 10kg x 12 reps = 120 KPI
        val entryA = WorkoutLog(
            id = 1,
            exerciseId = 1,
            timestamp = now - 1000,
            fieldValues = listOf(10f, 12f),
            kpi = 120f
        )

        // Entry B: 15kg x 8 reps = 120 KPI (Higher Field 1: 15 > 10)
        val entryB = WorkoutLog(
            id = 2,
            exerciseId = 1,
            timestamp = now - 500,
            fieldValues = listOf(15f, 8f),
            kpi = 120f
        )

        // Entry C: Lower KPI
        val entryC = WorkoutLog(
            id = 3,
            exerciseId = 1,
            timestamp = now,
            fieldValues = listOf(12f, 8f),
            kpi = 96f
        )

        val selected = GraphProcessor.selectTopLog(listOf(entryA, entryB, entryC))
        assertNotNull(selected)
        assertEquals("Entry B should win tie-breaker due to higher Field 1 (15 > 10)", entryB.id, selected!!.id)
    }

    @Test
    fun testSecondaryTieBreaker() {
        val now = System.currentTimeMillis()

        // Equal KPI (600), equal Field 1 (100) -> compare Field 2 (6 vs 5)
        val log1 = WorkoutLog(id = 1, exerciseId = 1, timestamp = now, fieldValues = listOf(100f, 5f, 1.2f), kpi = 600f)
        val log2 = WorkoutLog(id = 2, exerciseId = 1, timestamp = now, fieldValues = listOf(100f, 6f, 1.0f), kpi = 600f)

        val selected = GraphProcessor.selectTopLog(listOf(log1, log2))
        assertNotNull(selected)
        assertEquals(log2.id, selected!!.id)
    }

    @Test
    fun testGraphProcessorTimeRange() {
        val now = System.currentTimeMillis()
        // Two sets logged on same day: should select peak KPI 800
        val logsSameDay = listOf(
            WorkoutLog(id = 1, exerciseId = 1, timestamp = now, fieldValues = listOf(80f, 10f), kpi = 800f),
            WorkoutLog(id = 2, exerciseId = 1, timestamp = now - 3600000L, fieldValues = listOf(75f, 10f), kpi = 750f)
        )

        val points = GraphProcessor.processData(logsSameDay, TimeRange.CURRENT_WEEK)
        assertNotNull(points)
        assertEquals(1, points.size)
        assertEquals(800f, points[0].kpi, 0.001f)

        // Multiple entries across months for ALL_TIME
        val logsMultipleMonths = listOf(
            WorkoutLog(id = 1, exerciseId = 1, timestamp = now, fieldValues = listOf(80f, 10f), kpi = 800f),
            WorkoutLog(id = 2, exerciseId = 1, timestamp = now - 45L * 86400000L, fieldValues = listOf(70f, 10f), kpi = 700f)
        )
        // Verify ALL TimeRanges process successfully without format exceptions
        TimeRange.values().forEach { range ->
            val pts = GraphProcessor.processData(logsMultipleMonths, range)
            assertNotNull(pts)
        }
    }
}
