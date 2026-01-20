package com.runners.app.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.ceil

@Composable
fun RunningCalendarCard(
    runs: List<RunRecordUiModel>,
    modifier: Modifier = Modifier,
) {
    val runsByDate = remember(runs) { runs.groupBy { it.date } }
    val today = remember { LocalDate.now() }

    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val monthDays = remember(month) { computeMonthCells(month) }
    val monthTotals = remember(runsByDate, month) { computeMonthTotals(runsByDate, month) }
    val maxDistanceKm = remember(monthTotals) { monthTotals.values.maxOrNull() ?: 0.0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${month.year}년 ${month.monthValue}월",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(imageVector = Icons.Outlined.ChevronLeft, contentDescription = "이전 달")
                }
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = "다음 달")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val labels = listOf("월", "화", "수", "목", "금", "토", "일")
                for ((index, label) in labels.withIndex()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index >= 5) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            val rows = remember(monthDays) { ceil(monthDays.size / 7.0).toInt() }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val date = monthDays.getOrNull(cellIndex)
                            CalendarDayCell(
                                date = date,
                                today = today,
                                selected = selectedDate != null && selectedDate == date,
                                totalDistanceKm = if (date == null) 0.0 else (monthTotals[date] ?: 0.0),
                                maxDistanceKm = maxDistanceKm,
                                onClick = { clicked ->
                                    selectedDate = if (selectedDate == clicked) null else clicked
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            val selected = selectedDate
            if (selected != null) {
                val dayRuns = runsByDate[selected].orEmpty()
                val distanceKm = dayRuns.sumOf { it.distanceKm }
                val count = dayRuns.size
                Text(
                    text = "${selected.monthValue}/${selected.dayOfMonth} · ${formatKm(distanceKm)} · ${count}회",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "날짜를 누르면 해당 날짜 기록을 확인할 수 있어요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    today: LocalDate,
    selected: Boolean,
    totalDistanceKm: Double,
    maxDistanceKm: Double,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val isToday = date == today
    val enabled = date != null

    val base = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val t = if (maxDistanceKm <= 0.0) 0f else (totalDistanceKm / maxDistanceKm).toFloat().coerceIn(0f, 1f)
    val bgColor =
        if (!enabled) {
            Color.Transparent
        } else if (totalDistanceKm <= 0.0) {
            base.copy(alpha = 0.55f)
        } else {
            lerp(base, primary, 0.25f + 0.65f * t)
        }
    val distanceTextColor =
        if (!enabled || totalDistanceKm <= 0.0) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else if (t < 0.35f) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onPrimary
        }

    val borderColor =
        when {
            selected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.tertiary
            else -> Color.Transparent
        }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor)
            .border(width = if (borderColor == Color.Transparent) 0.dp else 1.5.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled) { onClick(requireNotNull(date)) }
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (date == null) return

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (totalDistanceKm > 0.0) {
                Text(
                    text = formatKmCompact(totalDistanceKm),
                    style = MaterialTheme.typography.labelSmall,
                    color = distanceTextColor,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }
}

private fun computeMonthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = (first.dayOfWeek.value - DayOfWeek.MONDAY.value).let { if (it < 0) it + 7 else it }
    val daysInMonth = month.lengthOfMonth()
    val totalCells = offset + daysInMonth
    val rows = ceil(totalCells / 7.0).toInt().coerceAtLeast(5)
    val cellCount = rows * 7

    val cells = ArrayList<LocalDate?>(cellCount)
    repeat(offset) { cells.add(null) }
    for (day in 1..daysInMonth) cells.add(month.atDay(day))
    while (cells.size < cellCount) cells.add(null)
    return cells
}

private fun computeMonthTotals(
    runsByDate: Map<LocalDate, List<RunRecordUiModel>>,
    month: YearMonth,
): Map<LocalDate, Double> {
    val totals = LinkedHashMap<LocalDate, Double>()
    val days = month.lengthOfMonth()
    for (day in 1..days) {
        val date = month.atDay(day)
        val distanceKm = runsByDate[date].orEmpty().sumOf { it.distanceKm }
        totals[date] = distanceKm
    }
    return totals
}

private fun formatKm(km: Double): String {
    if (km <= 0.0) return "0.0 km"
    return String.format(Locale.US, "%.1f km", km)
}

private fun formatKmCompact(km: Double): String {
    return if (km < 10.0) {
        String.format(Locale.US, "%.1f", km)
    } else {
        String.format(Locale.US, "%.0f", km)
    }
}
