package com.example.workouttracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.data.model.WorkoutLog
import com.example.workouttracker.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FireIntensityTier(val label: String) {
    NONE("Rest Day"),
    LOW("Light Spark"),
    MODERATE("Glowing Flame"),
    HIGH_PR("Blazing PR")
}

data class CalendarDayStat(
    val dateKey: String,
    val dayOfMonth: Int,
    val timestamp: Long,
    val logs: List<WorkoutLog>,
    val metricKpi: Float,
    val normalizedIntensity: Float, // 0f..1f
    val tier: FireIntensityTier,
    val isPersonalRecord: Boolean,
    val topLog: WorkoutLog?
)

object CalendarHeatmapProcessor {
    private val keyFormatter = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    fun formatDateKey(timestamp: Long): String {
        return keyFormatter.get()!!.format(Date(timestamp))
    }

    /**
     * Computes per-day statistics for a given year and month (0-based Calendar.MONTH).
     *
     * @param logs Workout logs (either all logs for Dashboard or exercise-filtered logs for Detail).
     * @param isExerciseSpecific If true, uses daily peak KPI (via GraphProcessor.selectTopLog).
     *                           If false, uses total daily KPI volume across all logged sets.
     */
    fun buildMonthStats(
        year: Int,
        month: Int,
        logs: List<WorkoutLog>,
        isExerciseSpecific: Boolean
    ): Map<Int, CalendarDayStat> {
        if (logs.isEmpty()) return emptyMap()

        val logsByDateKey = logs.groupBy { formatDateKey(it.timestamp) }

        // Compute metric value per date across all history so PRs and relative intensity are accurate
        val metricByDateKey = mutableMapOf<String, Float>()
        val topLogByDateKey = mutableMapOf<String, WorkoutLog?>()

        logsByDateKey.forEach { (key, dayLogs) ->
            val top = GraphProcessor.selectTopLog(dayLogs)
            topLogByDateKey[key] = top
            val metric = if (isExerciseSpecific) {
                top?.kpi ?: 0f
            } else {
                dayLogs.sumOf { it.kpi.toDouble() }.toFloat()
            }
            metricByDateKey[key] = metric
        }

        val globalMaxKpi = (metricByDateKey.values.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val globalMinActiveKpi = (metricByDateKey.values.filter { it > 0f }.minOrNull() ?: 0f)

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val result = mutableMapOf<Int, CalendarDayStat>()

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val key = formatDateKey(cal.timeInMillis)
            val dayLogs = logsByDateKey[key].orEmpty()
            if (dayLogs.isNotEmpty()) {
                val metric = metricByDateKey[key] ?: 0f
                val normalized = if (globalMaxKpi <= globalMinActiveKpi) {
                    // If all logged days have the exact same volume, treat multi-set or single max as high
                    if (dayLogs.size > 1) 0.85f else 0.65f
                } else {
                    val ratio = (metric - globalMinActiveKpi) / (globalMaxKpi - globalMinActiveKpi)
                    (0.22f + ratio * 0.78f).coerceIn(0.20f, 1.0f)
                }

                val isPr = metric >= globalMaxKpi * 0.98f && metric > 0f
                val tier = when {
                    isPr || normalized >= 0.78f -> FireIntensityTier.HIGH_PR
                    normalized >= 0.42f || dayLogs.size >= 2 -> FireIntensityTier.MODERATE
                    else -> FireIntensityTier.LOW
                }

                result[day] = CalendarDayStat(
                    dateKey = key,
                    dayOfMonth = day,
                    timestamp = cal.timeInMillis,
                    logs = dayLogs,
                    metricKpi = metric,
                    normalizedIntensity = normalized,
                    tier = tier,
                    isPersonalRecord = isPr,
                    topLog = topLogByDateKey[key]
                )
            }
        }

        return result
    }
}

@Composable
fun CalendarHeatmap(
    title: String,
    subtitle: String,
    logs: List<WorkoutLog>,
    isExerciseSpecific: Boolean,
    exercise: Exercise? = null,
    exercisesById: Map<Long, Exercise> = emptyMap(),
    modifier: Modifier = Modifier,
    testTagPrefix: String = "calendar_heatmap"
) {
    val glassColors = LocalGlassColors.current
    val nowCalendar = remember { Calendar.getInstance() }

    var displayedYear by remember { mutableIntStateOf(nowCalendar.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(nowCalendar.get(Calendar.MONTH)) }

    val monthStats = remember(displayedYear, displayedMonth, logs, isExerciseSpecific) {
        CalendarHeatmapProcessor.buildMonthStats(
            year = displayedYear,
            month = displayedMonth,
            logs = logs,
            isExerciseSpecific = isExerciseSpecific
        )
    }

    // Default selected day to the most recent active day in the displayed month, if any
    var selectedDayOfMonth by remember(displayedYear, displayedMonth, monthStats) {
        mutableStateOf(monthStats.keys.maxOrNull())
    }

    val monthHeaderText = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    }

    val calendarGridInfo = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Monday-first offset (0 = Mon .. 6 = Sun)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmptyCells = (dayOfWeek + 5) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(leadingEmptyCells, daysInMonth)
    }

    val activeDaysCount = monthStats.size
    val monthPeakKpi = monthStats.values.maxOfOrNull { it.metricKpi } ?: 0f

    // Infinite transition for glowing fire animations
    val infiniteTransition = rememberInfiniteTransition(label = "fire_heatmap_glow")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )
    val auraAlphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha_pulse"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Header: Title + Active Days summary badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        glassColors.primary.copy(alpha = 0.38f),
                                        glassColors.primary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(1.dp, glassColors.primary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = glassColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = glassColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = glassColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                GlassBadge(
                    text = "$activeDaysCount Active ${if (activeDaysCount == 1) "Day" else "Days"}",
                    backgroundColor = glassColors.primary.copy(alpha = 0.18f),
                    textColor = glassColors.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(glassColors.surfaceGlassVariant)
                    .border(
                        0.8.dp,
                        glassColors.borderGlass.copy(alpha = 0.28f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (displayedMonth == 0) {
                            displayedMonth = 11
                            displayedYear -= 1
                        } else {
                            displayedMonth -= 1
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("${testTagPrefix}_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = glassColors.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = monthHeaderText,
                        color = glassColors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val isCurrentMonth = displayedYear == nowCalendar.get(Calendar.YEAR) &&
                        displayedMonth == nowCalendar.get(Calendar.MONTH)
                    if (!isCurrentMonth) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(glassColors.primary.copy(alpha = 0.22f))
                                .clickable {
                                    displayedYear = nowCalendar.get(Calendar.YEAR)
                                    displayedMonth = nowCalendar.get(Calendar.MONTH)
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("${testTagPrefix}_today_btn")
                        ) {
                            Text(
                                text = "Today",
                                color = glassColors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (displayedMonth == 11) {
                            displayedMonth = 0
                            displayedYear += 1
                        } else {
                            displayedMonth += 1
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("${testTagPrefix}_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = glassColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers (Mon .. Sun)
            val weekDays = remember { listOf("M", "T", "W", "T", "F", "S", "S") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weekDays.forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = glassColors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar Grid Rows
            val (leadingEmpty, daysInMonth) = calendarGridInfo
            val totalCells = leadingEmpty + daysInMonth
            val rowsCount = (totalCells + 6) / 7

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_grid"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - leadingEmpty + 1
                            if (dayNumber in 1..daysInMonth) {
                                val dayStat = monthStats[dayNumber]
                                val isToday = displayedYear == nowCalendar.get(Calendar.YEAR) &&
                                    displayedMonth == nowCalendar.get(Calendar.MONTH) &&
                                    dayNumber == nowCalendar.get(Calendar.DAY_OF_MONTH)
                                val isSelected = selectedDayOfMonth == dayNumber

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    dayStat = dayStat,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    flamePulse = flamePulse,
                                    auraAlphaPulse = auraAlphaPulse,
                                    onClick = {
                                        selectedDayOfMonth = if (selectedDayOfMonth == dayNumber) {
                                            null
                                        } else {
                                            dayNumber
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.92f)
                                        .testTag("${testTagPrefix}_day_$dayNumber")
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.92f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Intensity Scale Legend
            FireIntensityLegend(
                monthPeakKpi = monthPeakKpi,
                isExerciseSpecific = isExerciseSpecific
            )

            // Selected Day Frosted Tooltip Breakdown
            val selectedStat = selectedDayOfMonth?.let { monthStats[it] }
            AnimatedVisibility(
                visible = selectedStat != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedStat?.let { stat ->
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectedDayDetailPanel(
                        stat = stat,
                        isExerciseSpecific = isExerciseSpecific,
                        exercise = exercise,
                        exercisesById = exercisesById
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    dayStat: CalendarDayStat?,
    isToday: Boolean,
    isSelected: Boolean,
    flamePulse: Float,
    auraAlphaPulse: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val tier = dayStat?.tier ?: FireIntensityTier.NONE
    val intensity = dayStat?.normalizedIntensity ?: 0f

    // Dynamic colors & borders scaled by workout volume
    val cellShape = RoundedCornerShape(11.dp)

    val cellBackgroundBrush = when (tier) {
        FireIntensityTier.NONE -> Brush.verticalGradient(
            colors = listOf(
                glassColors.surfaceGlassVariant.copy(alpha = 0.25f),
                glassColors.surfaceGlassVariant.copy(alpha = 0.12f)
            )
        )
        FireIntensityTier.LOW -> Brush.verticalGradient(
            colors = listOf(
                glassColors.primary.copy(alpha = 0.14f),
                glassColors.surfaceGlassVariant.copy(alpha = 0.30f)
            )
        )
        FireIntensityTier.MODERATE -> Brush.verticalGradient(
            colors = listOf(
                glassColors.primary.copy(alpha = 0.26f),
                glassColors.primaryVariant.copy(alpha = 0.18f)
            )
        )
        FireIntensityTier.HIGH_PR -> Brush.verticalGradient(
            colors = listOf(
                glassColors.primary.copy(alpha = 0.45f),
                glassColors.secondary.copy(alpha = 0.28f)
            )
        )
    }

    val borderStroke = when {
        isSelected -> BorderStroke(1.8.dp, glassColors.primary)
        tier == FireIntensityTier.HIGH_PR -> BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                colors = listOf(
                    glassColors.primary,
                    Color.White.copy(alpha = 0.75f),
                    glassColors.secondary
                )
            )
        )
        tier == FireIntensityTier.MODERATE -> BorderStroke(
            1.dp,
            glassColors.primary.copy(alpha = 0.55f)
        )
        tier == FireIntensityTier.LOW -> BorderStroke(
            0.8.dp,
            glassColors.primary.copy(alpha = 0.32f)
        )
        isToday -> BorderStroke(1.dp, glassColors.textSecondary.copy(alpha = 0.6f))
        else -> BorderStroke(0.6.dp, glassColors.borderGlass.copy(alpha = 0.18f))
    }

    Box(
        modifier = modifier
            .clip(cellShape)
            .background(cellBackgroundBrush)
            .border(borderStroke, cellShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = glassColors.primary),
                onClick = onClick
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient blurred aura behind the fire icon for active workout days
        if (tier != FireIntensityTier.NONE) {
            val auraScale = when (tier) {
                FireIntensityTier.LOW -> 0.65f
                FireIntensityTier.MODERATE -> 0.90f * flamePulse
                FireIntensityTier.HIGH_PR -> 1.18f * flamePulse
                else -> 0f
            }
            val auraAlpha = when (tier) {
                FireIntensityTier.LOW -> 0.22f
                FireIntensityTier.MODERATE -> 0.42f * auraAlphaPulse
                FireIntensityTier.HIGH_PR -> 0.72f * auraAlphaPulse
                else -> 0f
            }

            // Frosted blur layer + radial neon aura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(auraScale)
                    .blur(if (tier == FireIntensityTier.HIGH_PR) 8.dp else 4.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glassColors.primary.copy(alpha = auraAlpha),
                                glassColors.secondary.copy(alpha = auraAlpha * 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Day number at top
            Text(
                text = dayNumber.toString(),
                color = when {
                    tier == FireIntensityTier.HIGH_PR -> glassColors.textPrimary
                    tier != FireIntensityTier.NONE -> glassColors.textPrimary.copy(alpha = 0.92f)
                    isToday -> glassColors.primary
                    else -> glassColors.textSecondary.copy(alpha = 0.65f)
                },
                fontSize = 10.sp,
                fontWeight = if (tier != FireIntensityTier.NONE || isToday) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Glowing Fire Animation / Vector Icon for days with logged workouts
            if (tier != FireIntensityTier.NONE) {
                GlowingFireIcon(
                    tier = tier,
                    intensity = intensity,
                    flamePulse = flamePulse,
                    auraAlphaPulse = auraAlphaPulse,
                    primaryColor = glassColors.primary,
                    secondaryColor = glassColors.secondary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(bottom = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun GlowingFireIcon(
    tier: FireIntensityTier,
    intensity: Float,
    flamePulse: Float,
    auraAlphaPulse: Float,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val iconScale = when (tier) {
        FireIntensityTier.LOW -> 0.78f
        FireIntensityTier.MODERATE -> 0.94f * (0.96f + 0.04f * flamePulse)
        FireIntensityTier.HIGH_PR -> 1.08f * flamePulse
        FireIntensityTier.NONE -> 0f
    }

    val iconAlpha = when (tier) {
        FireIntensityTier.LOW -> 0.58f
        FireIntensityTier.MODERATE -> 0.86f
        FireIntensityTier.HIGH_PR -> 1.0f
        FireIntensityTier.NONE -> 0f
    }

    // Core flame tint blends theme primary with warm ember/white hot core for PRs
    val flameTint = when (tier) {
        FireIntensityTier.LOW -> primaryColor.copy(alpha = iconAlpha)
        FireIntensityTier.MODERATE -> primaryColor.copy(alpha = iconAlpha)
        FireIntensityTier.HIGH_PR -> primaryColor
        FireIntensityTier.NONE -> Color.Transparent
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Particle / Halo Canvas for Moderate & High PR tiers
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 2f

            if (tier == FireIntensityTier.MODERATE || tier == FireIntensityTier.HIGH_PR) {
                // Soft glowing halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(
                                alpha = if (tier == FireIntensityTier.HIGH_PR) {
                                    0.65f * auraAlphaPulse
                                } else {
                                    0.35f * auraAlphaPulse
                                }
                            ),
                            secondaryColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * (if (tier == FireIntensityTier.HIGH_PR) 1.45f else 1.15f)
                    ),
                    radius = baseRadius * (if (tier == FireIntensityTier.HIGH_PR) 1.45f else 1.15f),
                    center = center
                )
            }

            // Blazing neon ember sparks on High KPI / PR days
            if (tier == FireIntensityTier.HIGH_PR) {
                val sparkOffset = (flamePulse - 0.92f) * 18f
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f * auraAlphaPulse),
                    radius = 1.6.dp.toPx(),
                    center = Offset(center.x - baseRadius * 0.55f, center.y - baseRadius * 0.5f - sparkOffset)
                )
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.9f * auraAlphaPulse),
                    radius = 1.4.dp.toPx(),
                    center = Offset(center.x + baseRadius * 0.6f, center.y - baseRadius * 0.35f - sparkOffset * 0.8f)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.95f * auraAlphaPulse),
                    radius = 1.3.dp.toPx(),
                    center = Offset(center.x, center.y - baseRadius * 0.85f - sparkOffset * 0.6f)
                )
            }
        }

        // Vector Fire Icon
        Icon(
            imageVector = if (tier == FireIntensityTier.LOW) {
                Icons.Default.Whatshot
            } else {
                Icons.Default.LocalFireDepartment
            },
            contentDescription = tier.label,
            tint = flameTint,
            modifier = Modifier
                .fillMaxSize()
                .scale(iconScale)
        )

        // Bright inner core highlight for Blazing PR tier
        if (tier == FireIntensityTier.HIGH_PR) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.65f * auraAlphaPulse),
                modifier = Modifier
                    .fillMaxSize(0.55f)
                    .scale(flamePulse)
            )
        }
    }
}

@Composable
private fun FireIntensityLegend(
    monthPeakKpi: Float,
    isExerciseSpecific: Boolean
) {
    val glassColors = LocalGlassColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(
                label = "Spark",
                alpha = 0.45f,
                primaryColor = glassColors.primary,
                textColor = glassColors.textSecondary
            )
            LegendItem(
                label = "Flame",
                alpha = 0.78f,
                primaryColor = glassColors.primary,
                textColor = glassColors.textSecondary
            )
            LegendItem(
                label = "Blazing PR",
                alpha = 1.0f,
                primaryColor = glassColors.primary,
                textColor = glassColors.textPrimary,
                isBold = true
            )
        }

        if (monthPeakKpi > 0f) {
            Text(
                text = if (isExerciseSpecific) {
                    "Month Peak: ${String.format(Locale.US, "%.1f", monthPeakKpi)}"
                } else {
                    "Peak Vol: ${String.format(Locale.US, "%.0f", monthPeakKpi)}"
                },
                color = glassColors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    alpha: Float,
    primaryColor: Color,
    textColor: Color,
    isBold: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = primaryColor.copy(alpha = alpha),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SelectedDayDetailPanel(
    stat: CalendarDayStat,
    isExerciseSpecific: Boolean,
    exercise: Exercise?,
    exercisesById: Map<Long, Exercise>
) {
    val glassColors = LocalGlassColors.current
    val fullDateStr = remember(stat.timestamp) {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(Date(stat.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(glassColors.surfaceGlassVariant)
            .border(
                1.dp,
                glassColors.primary.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = fullDateStr,
                    color = glassColors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stat.logs.size} ${if (stat.logs.size == 1) "set" else "sets"} logged",
                    color = glassColors.textSecondary,
                    fontSize = 11.sp
                )
            }

            GlassBadge(
                text = "${stat.tier.label} • ${
                    if (isExerciseSpecific) "Peak KPI" else "Vol KPI"
                }: ${String.format(Locale.US, "%.1f", stat.metricKpi)}",
                backgroundColor = glassColors.primary.copy(alpha = 0.22f),
                textColor = glassColors.primary
            )
        }

        if (isExerciseSpecific && exercise != null && stat.topLog != null) {
            Spacer(modifier = Modifier.height(6.dp))
            val breakdown = exercise.fieldLabels.mapIndexed { idx, label ->
                val v = stat.topLog.fieldValues.getOrNull(idx) ?: 0f
                val formatted = if (v % 1f == 0f) v.toInt().toString() else String.format(Locale.US, "%.1f", v)
                "$label: $formatted"
            }.joinToString(" • ")

            Text(
                text = "Top Set: $breakdown",
                color = glassColors.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        } else if (!isExerciseSpecific && exercisesById.isNotEmpty()) {
            val distinctExerciseNames = stat.logs
                .mapNotNull { exercisesById[it.exerciseId]?.name }
                .distinct()
            if (distinctExerciseNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Exercises: ${distinctExerciseNames.joinToString(", ")}",
                    color = glassColors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
