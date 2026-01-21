@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.runners.app.records

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

// --- 메인 컴포넌트 ---

@Composable
fun RunningCalendarCard(
    runs: List<RunRecordUiModel>,
    loadDetails: (suspend (RunRecordUiModel) -> RunRecordDetails)? = null,
    modifier: Modifier = Modifier,
) {
    val runsByDate = remember(runs) { runs.groupBy { it.date } }
    val today = remember { LocalDate.now() }

    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val monthDays = remember(month) { computeMonthCells(month) }
    val monthTotals = remember(runsByDate, month) { computeMonthTotals(runsByDate, month) }
    val maxDistanceKm = remember(monthTotals) { monthTotals.values.maxOrNull() ?: 0.0 }
    val zoneId = remember { ZoneId.systemDefault() }
    val detailsCache = remember { mutableStateMapOf<String, RunRecordDetails>() }
    val bringDetailsIntoViewRequester = remember { BringIntoViewRequester() }

    // 디자인: 카드 모양과 그림자를 더 부드럽게 처리
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x1A000000)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(), // 크기 변경 시 부드러운 애니메이션
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 캘린더 헤더 (월 이동 및 요약)
            CalendarHeader(
                currentMonth = month,
                selectedDate = selectedDate,
                runsByDate = runsByDate,
                onPrevClick = { month = month.minusMonths(1) },
                onNextClick = { month = month.plusMonths(1) }
            )

            // 2. 요일 헤더
            WeekDaysHeader()

            // 3. 날짜 그리드 (히트맵)
            CalendarGrid(
                monthDays = monthDays,
                today = today,
                selectedDate = selectedDate,
                monthTotals = monthTotals,
                maxDistanceKm = maxDistanceKm,
                onDateClick = { clicked ->
                    selectedDate = if (selectedDate == clicked) null else clicked
                }
            )

            // 4. 선택된 날짜의 러닝 리스트
            val selected = selectedDate
            if (selected != null) {
                val dayRuns = runsByDate[selected].orEmpty().sortedByDescending { it.startTime }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                LaunchedEffect(selected) {
                    bringDetailsIntoViewRequester.bringIntoView()
                }

                DayRunsList(
                    date = selected,
                    runs = dayRuns,
                    zoneId = zoneId,
                    detailsCache = detailsCache,
                    loadDetails = loadDetails,
                    modifier = Modifier.bringIntoViewRequester(bringDetailsIntoViewRequester),
                )
            }
        }
    }
}

// --- 하위 컴포넌트들 ---

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    runsByDate: Map<LocalDate, List<RunRecordUiModel>>,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val selected = selectedDate
            if (selected != null) {
                val dayRuns = runsByDate[selected].orEmpty()
                val distanceKm = dayRuns.sumOf { it.distanceKm }
                Text(
                    text = "${selected.dayOfMonth}일 · ${formatKm(distanceKm)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "기록을 확인할 날짜 선택",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NavigationIconBtn(Icons.Outlined.ChevronLeft, onPrevClick)
            NavigationIconBtn(Icons.Outlined.ChevronRight, onNextClick)
        }
    }
}

@Composable
private fun NavigationIconBtn(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WeekDaysHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val labels = listOf("월", "화", "수", "목", "금", "토", "일")
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (index >= 5) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    monthDays: List<LocalDate?>,
    today: LocalDate,
    selectedDate: LocalDate?,
    monthTotals: Map<LocalDate, Double>,
    maxDistanceKm: Double,
    onDateClick: (LocalDate) -> Unit
) {
    val rows = remember(monthDays) { ceil(monthDays.size / 7.0).toInt() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (colIndex in 0 until 7) {
                    val cellIndex = rowIndex * 7 + colIndex
                    val date = monthDays.getOrNull(cellIndex)

                    if (date != null) {
                        CalendarDayCell(
                            date = date,
                            today = today,
                            isSelected = selectedDate == date,
                            totalDistanceKm = monthTotals[date] ?: 0.0,
                            maxDistanceKm = maxDistanceKm,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    today: LocalDate,
    isSelected: Boolean,
    totalDistanceKm: Double,
    maxDistanceKm: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == today
    val hasRun = totalDistanceKm > 0.0

    // 히트맵 색상 로직: 운동량이 많을수록 Primary 색상이 진해짐
    val backgroundColor = if (hasRun) {
        val intensity = if (maxDistanceKm > 0) (totalDistanceKm / maxDistanceKm).toFloat().coerceIn(0.1f, 1f) else 0f
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f + (intensity * 0.9f))
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val textColor = if (hasRun) {
        // 배경이 진하면 흰 글씨, 연하면 검은 글씨 (간단한 로직)
        if ((totalDistanceKm / maxDistanceKm) > 0.5) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    // 선택 시 테두리 스타일
    val borderWidth = if (isSelected) 2.dp else 0.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = modifier
            .aspectRatio(0.85f) // 약간 세로로 긴 비율이 더 현대적임
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 날짜 숫자
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isToday && !hasRun) {
                // 오늘인데 기록 없으면 동그라미 표시
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday && !hasRun) MaterialTheme.colorScheme.primary else textColor
            )
        }

        // 거리 표시 (작게)
        if (hasRun) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatKmCompact(totalDistanceKm),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = textColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DayRunsList(
    date: LocalDate,
    runs: List<RunRecordUiModel>,
    zoneId: ZoneId,
    detailsCache: MutableMap<String, RunRecordDetails>,
    loadDetails: (suspend (RunRecordUiModel) -> RunRecordDetails)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (runs.isEmpty()) {
            EmptyStateMessage(date)
            return
        }

        runs.forEach { run ->
            DayRunRow(
                run = run,
                zoneId = zoneId,
                detailsCache = detailsCache,
                loadDetails = loadDetails,
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(date: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${date.monthValue}월 ${date.dayOfMonth}일은 휴식일이었네요 💤",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayRunRow(
    run: RunRecordUiModel,
    zoneId: ZoneId,
    detailsCache: MutableMap<String, RunRecordDetails>,
    loadDetails: (suspend (RunRecordUiModel) -> RunRecordDetails)?,
) {
    val key = remember(run) { run.cacheKey() }
    val details = detailsCache[key]

    LaunchedEffect(key, loadDetails) {
        val loader = loadDetails ?: return@LaunchedEffect
        if (!detailsCache.containsKey(key)) {
            val loaded = runCatching { loader(run) }.getOrElse { RunRecordDetails() }
            detailsCache[key] = loaded
        }
    }

    // 디자인: 기록 카드를 더 세련되게 (왼쪽: 시간/아이콘, 오른쪽: 핵심 데이터)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer, // 밝은 회색톤 배경
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. 왼쪽: 아이콘과 시간
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = run.startTime.toLocalTimeText(zoneId),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 거리 (강조)
                Text(
                    text = "${formatKm(run.distanceKm)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 2. 오른쪽: 세부 스탯 (수직 정렬)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 시간
                StatRowSimple(
                    icon = Icons.Rounded.AccessTime,
                    text = run.durationMinutes?.let { formatMinutes(it) } ?: "-"
                )

                // 페이스 or 칼로리 (데이터 있으면)
                details?.let {
                    if ((it.caloriesKcal ?: 0.0) > 0) {
                        StatRowSimple(
                            icon = Icons.Rounded.LocalFireDepartment,
                            text = String.format(Locale.US, "%.0f kcal", it.caloriesKcal)
                        )
                    }
                    if (it.avgHeartRateBpm != null) {
                        StatRowSimple(
                            icon = Icons.Rounded.Favorite,
                            text = "${it.avgHeartRateBpm} bpm"
                        )
                    } else if (it.cadenceSpm != null) {
                        StatRowSimple(
                            icon = Icons.Rounded.Speed,
                            text = "${it.cadenceSpm} spm"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRowSimple(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

// --- 유틸리티 함수 (기존 로직 유지) ---

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
    return if (km < 10.0) String.format(Locale.US, "%.1f", km) else String.format(Locale.US, "%.0f", km)
}

private fun RunRecordUiModel.cacheKey(): String {
    return "${startTime.toEpochMilli()}-${endTime.toEpochMilli()}-${dataOriginPackageName.lowercase(Locale.US)}"
}

private fun Instant.toLocalTimeText(zoneId: ZoneId): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN)
    return atZone(zoneId).toLocalTime().format(formatter)
}

private fun formatMinutes(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}