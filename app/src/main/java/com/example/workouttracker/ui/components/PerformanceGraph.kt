package com.example.workouttracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.WorkoutLog
import com.example.workouttracker.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class TimeRange(val displayName: String) {
    CURRENT_WEEK("Current Week"),
    CURRENT_MONTH("Current Month"),
    TWO_MONTHS("2 Months"),
    THREE_MONTHS("3 Months"),
    SIX_MONTHS("6 Months"),
    YTD("Year-to-Date"),
    ALL_TIME("All Time")
}

data class GraphPoint(
    val key: String,
    val xLabel: String,
    val dateSubtitle: String,
    val kpi: Float,
    val topLog: WorkoutLog,
    val showTick: Boolean = true
)

object GraphProcessor {

    /**
     * Tie-breaking daily peak selection algorithm:
     * 1. Max KPI
     * 2. If equal, higher Field 1
     * 3. If equal, higher Field 2, Field 3...
     * 4. If all equal, most recent timestamp
     */
    fun selectTopLog(logs: List<WorkoutLog>): WorkoutLog? {
        if (logs.isEmpty()) return null
        return logs.maxWithOrNull { a, b ->
            val kpiDiff = a.kpi.compareTo(b.kpi)
            if (kpiDiff != 0) return@maxWithOrNull kpiDiff

            val maxFields = maxOf(a.fieldValues.size, b.fieldValues.size)
            for (i in 0 until maxFields) {
                val valA = a.fieldValues.getOrNull(i) ?: 0f
                val valB = b.fieldValues.getOrNull(i) ?: 0f
                val fieldDiff = valA.compareTo(valB)
                if (fieldDiff != 0) return@maxWithOrNull fieldDiff
            }

            a.timestamp.compareTo(b.timestamp)
        }
    }

    fun processData(logs: List<WorkoutLog>, range: TimeRange): List<GraphPoint> {
        if (logs.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val startTime: Long = when (range) {
            TimeRange.CURRENT_WEEK -> {
                calendar.timeInMillis = now
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeRange.CURRENT_MONTH -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeRange.TWO_MONTHS -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -2)
                calendar.timeInMillis
            }
            TimeRange.THREE_MONTHS -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }
            TimeRange.SIX_MONTHS -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -6)
                calendar.timeInMillis
            }
            TimeRange.YTD -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            TimeRange.ALL_TIME -> 0L
        }

        // Filter logs within time range
        val filteredLogs = logs.filter { it.timestamp >= startTime }
        if (filteredLogs.isEmpty()) return emptyList()

        return when (range) {
            TimeRange.CURRENT_WEEK -> {
                // Group by Day of Week (Mon, Tue, Wed...)
                val dayFormat = SimpleDateFormat("EEE", Locale.US)
                val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                filteredLogs.groupBy { keyFormat.format(Date(it.timestamp)) }
                    .toSortedMap()
                    .mapNotNull { (_, dayLogs) ->
                        val top = selectTopLog(dayLogs) ?: return@mapNotNull null
                        GraphPoint(
                            key = keyFormat.format(Date(top.timestamp)),
                            xLabel = dayFormat.format(Date(top.timestamp)),
                            dateSubtitle = fullDateFormat.format(Date(top.timestamp)),
                            kpi = top.kpi,
                            topLog = top,
                            showTick = true
                        )
                    }
            }

            TimeRange.CURRENT_MONTH -> {
                // Group by Date, ticks every 4-5 days
                val dateFormat = SimpleDateFormat("MMM d", Locale.US)
                val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                val dailyPoints = filteredLogs.groupBy { keyFormat.format(Date(it.timestamp)) }
                    .toSortedMap()
                    .mapNotNull { (_, dayLogs) ->
                        val top = selectTopLog(dayLogs) ?: return@mapNotNull null
                        GraphPoint(
                            key = keyFormat.format(Date(top.timestamp)),
                            xLabel = dateFormat.format(Date(top.timestamp)),
                            dateSubtitle = fullDateFormat.format(Date(top.timestamp)),
                            kpi = top.kpi,
                            topLog = top
                        )
                    }

                // Show ticks every 4-5 days or for first, middle, last
                dailyPoints.mapIndexed { index, point ->
                    val showTick = index == 0 || index == dailyPoints.lastIndex || (index % 4 == 0)
                    point.copy(showTick = showTick)
                }
            }

            TimeRange.TWO_MONTHS, TimeRange.THREE_MONTHS -> {
                // Group by Week (Weekly peak KPI)
                val weekKeyFormat = SimpleDateFormat("yyyy-'W'ww", Locale.US)
                val weekLabelFormat = SimpleDateFormat("MMM dd", Locale.US)
                val fullDateFormat = SimpleDateFormat("'Week of' MMM d, yyyy", Locale.US)

                val weeklyPoints = filteredLogs.groupBy { weekKeyFormat.format(Date(it.timestamp)) }
                    .toSortedMap()
                    .mapNotNull { (_, weekLogs) ->
                        val top = selectTopLog(weekLogs) ?: return@mapNotNull null
                        GraphPoint(
                            key = weekKeyFormat.format(Date(top.timestamp)),
                            xLabel = weekLabelFormat.format(Date(top.timestamp)),
                            dateSubtitle = fullDateFormat.format(Date(top.timestamp)),
                            kpi = top.kpi,
                            topLog = top
                        )
                    }

                val tickStep = if (range == TimeRange.TWO_MONTHS) 1 else 2
                weeklyPoints.mapIndexed { index, point ->
                    point.copy(showTick = (index % tickStep == 0) || index == weeklyPoints.lastIndex)
                }
            }

            TimeRange.SIX_MONTHS, TimeRange.YTD -> {
                // Group by Month (Monthly peak KPI)
                val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
                val monthLabelFormat = SimpleDateFormat("MMM", Locale.US)
                val fullDateFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

                filteredLogs.groupBy { monthKeyFormat.format(Date(it.timestamp)) }
                    .toSortedMap()
                    .mapNotNull { (_, monthLogs) ->
                        val top = selectTopLog(monthLogs) ?: return@mapNotNull null
                        GraphPoint(
                            key = monthKeyFormat.format(Date(top.timestamp)),
                            xLabel = monthLabelFormat.format(Date(top.timestamp)),
                            dateSubtitle = fullDateFormat.format(Date(top.timestamp)),
                            kpi = top.kpi,
                            topLog = top,
                            showTick = true
                        )
                    }
            }

            TimeRange.ALL_TIME -> {
                // Monthly peak (or quarterly if long span)
                val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
                val monthYearLabelFormat = SimpleDateFormat("MMM ''yy", Locale.US)
                val fullDateFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

                val points = filteredLogs.groupBy { monthKeyFormat.format(Date(it.timestamp)) }
                    .toSortedMap()
                    .mapNotNull { (_, monthLogs) ->
                        val top = selectTopLog(monthLogs) ?: return@mapNotNull null
                        GraphPoint(
                            key = monthKeyFormat.format(Date(top.timestamp)),
                            xLabel = monthYearLabelFormat.format(Date(top.timestamp)),
                            dateSubtitle = fullDateFormat.format(Date(top.timestamp)),
                            kpi = top.kpi,
                            topLog = top
                        )
                    }

                // Show 6-8 evenly spaced ticks max
                val total = points.size
                val step = if (total > 8) (total / 6).coerceAtLeast(1) else 1
                points.mapIndexed { index, pt ->
                    pt.copy(showTick = (index % step == 0) || index == points.lastIndex)
                }
            }
        }
    }
}

@Composable
fun PerformanceGraph(
    exercise: Exercise,
    logs: List<WorkoutLog>,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val textMeasurer = rememberTextMeasurer()

    val points = remember(logs, selectedRange) {
        GraphProcessor.processData(logs, selectedRange)
    }

    var selectedPointIndex by remember(points) {
        mutableStateOf(if (points.isNotEmpty()) points.lastIndex else -1)
    }

    val selectedPoint = points.getOrNull(selectedPointIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("performance_graph_container")
    ) {
        // Range selection chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.values().forEach { range ->
                GlassFilterChip(
                    text = range.displayName,
                    isSelected = range == selectedRange,
                    onClick = { onRangeSelected(range) },
                    modifier = Modifier.testTag("chip_range_${range.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Glass container for Chart Canvas
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            if (points.isEmpty()) {
                // Empty state within range
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No data",
                        tint = glassColors.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No workouts logged in this period",
                        color = glassColors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log a set to view the performance trajectory",
                        color = glassColors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Graph Canvas
                    val primaryColor = glassColors.primary
                    val textSecColor = glassColors.textSecondary
                    val gridColor = glassColors.borderGlass.copy(alpha = 0.2f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 14.dp, end = 18.dp, top = 20.dp, bottom = 28.dp)
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width
                                    if (points.size == 1) {
                                        selectedPointIndex = 0
                                    } else {
                                        val stepX = width / (points.size - 1).coerceAtLeast(1)
                                        val clickedIndex = (tapOffset.x / stepX).toInt()
                                            .coerceIn(0, points.lastIndex)
                                        // Pick closest point
                                        val prevDist = abs((clickedIndex * stepX) - tapOffset.x)
                                        val nextDist = if (clickedIndex < points.lastIndex) {
                                            abs(((clickedIndex + 1) * stepX) - tapOffset.x)
                                        } else Float.MAX_VALUE

                                        selectedPointIndex = if (nextDist < prevDist) {
                                            clickedIndex + 1
                                        } else {
                                            clickedIndex
                                        }
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val maxKpi = (points.maxOfOrNull { it.kpi } ?: 100f).coerceAtLeast(1f)
                        val minKpi = 0f
                        val kpiRange = (maxKpi - minKpi) * 1.15f // 15% headroom

                        // Draw horizontal grid lines (3 levels)
                        val gridLevels = 3
                        for (i in 0..gridLevels) {
                            val y = canvasHeight * (1f - (i.toFloat() / gridLevels))
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        // Compute point coordinates
                        val offsets = points.mapIndexed { index, pt ->
                            val x = if (points.size == 1) {
                                canvasWidth / 2f
                            } else {
                                (index.toFloat() / (points.size - 1)) * canvasWidth
                            }
                            val normalizedY = ((pt.kpi - minKpi) / kpiRange).coerceIn(0f, 1f)
                            val y = canvasHeight * (1f - normalizedY)
                            Offset(x, y)
                        }

                        // Draw curved path
                        if (offsets.size > 1) {
                            val linePath = Path()
                            val fillPath = Path()

                            linePath.moveTo(offsets[0].x, offsets[0].y)
                            fillPath.moveTo(offsets[0].x, canvasHeight)
                            fillPath.lineTo(offsets[0].x, offsets[0].y)

                            for (i in 0 until offsets.size - 1) {
                                val current = offsets[i]
                                val next = offsets[i + 1]
                                val controlX1 = current.x + (next.x - current.x) / 2f
                                val controlY1 = current.y
                                val controlX2 = current.x + (next.x - current.x) / 2f
                                val controlY2 = next.y

                                linePath.cubicTo(
                                    controlX1, controlY1,
                                    controlX2, controlY2,
                                    next.x, next.y
                                )
                                fillPath.cubicTo(
                                    controlX1, controlY1,
                                    controlX2, controlY2,
                                    next.x, next.y
                                )
                            }

                            fillPath.lineTo(offsets.last().x, canvasHeight)
                            fillPath.close()

                            // Draw gradient fill beneath the curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.35f),
                                        primaryColor.copy(alpha = 0.05f)
                                    )
                                )
                            )

                            // Draw stroke
                            drawPath(
                                path = linePath,
                                color = primaryColor,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else if (offsets.size == 1) {
                            // Single point baseline
                            drawLine(
                                color = primaryColor.copy(alpha = 0.4f),
                                start = Offset(0f, offsets[0].y),
                                end = Offset(canvasWidth, offsets[0].y),
                                strokeWidth = 2f
                            )
                        }

                        // Draw node points and ticks
                        offsets.forEachIndexed { index, offset ->
                            val pt = points[index]
                            val isSelected = index == selectedPointIndex

                            if (isSelected) {
                                // Vertical guide line
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.6f),
                                    start = Offset(offset.x, 0f),
                                    end = Offset(offset.x, canvasHeight),
                                    strokeWidth = 1.5f
                                )
                                // Glowing outer pulse
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.3f),
                                    radius = 12.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 7.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.5.dp.toPx(),
                                    center = offset
                                )
                            } else {
                                drawCircle(
                                    color = primaryColor,
                                    radius = 4.5.dp.toPx(),
                                    center = offset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = offset
                                )
                            }

                            // X-axis label
                            if (pt.showTick) {
                                val textResult = textMeasurer.measure(
                                    text = pt.xLabel,
                                    style = TextStyle(
                                        color = if (isSelected) primaryColor else textSecColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                val labelX = (offset.x - textResult.size.width / 2f)
                                    .coerceIn(0f, canvasWidth - textResult.size.width)
                                val labelY = canvasHeight + 8.dp.toPx()

                                drawText(
                                    textLayoutResult = textResult,
                                    topLeft = Offset(labelX, labelY)
                                )
                            }
                        }
                    }

                    // Y-Axis Formula Title (top left)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(glassColors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KPI: ${exercise.formulaDisplay}",
                            color = glassColors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Interactive Frosted Tooltip Detail Card
        AnimatedVisibility(
            visible = selectedPoint != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedPoint?.let { pt ->
                Spacer(modifier = Modifier.height(10.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("graph_tooltip_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pt.dateSubtitle,
                                    color = glassColors.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Top Performance Set",
                                    color = glassColors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(glassColors.primary.copy(alpha = 0.2f))
                                    .border(
                                        1.dp,
                                        glassColors.primary.copy(alpha = 0.4f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = glassColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "KPI: ${String.format(Locale.US, "%.1f", pt.kpi)}",
                                    color = glassColors.primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Individual Field Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            exercise.fieldLabels.forEachIndexed { i, label ->
                                val value = pt.topLog.fieldValues.getOrNull(i) ?: 0f
                                val formattedValue = if (value % 1f == 0f) {
                                    value.toInt().toString()
                                } else {
                                    String.format(Locale.US, "%.1f", value)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(glassColors.surfaceGlassVariant)
                                        .border(
                                            0.8.dp,
                                            glassColors.borderGlass.copy(alpha = 0.3f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = label,
                                            color = glassColors.textSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formattedValue,
                                            color = glassColors.textPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
