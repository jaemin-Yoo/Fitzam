package com.jaemin.fitzam.ui.dzam

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.jaemin.fitzam.ui.theme.FitzamTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class DZamCalendarEvent(
    val text: String,
    val backgroundColor: Color
)

private const val INITIAL_PAGE = 600
private const val TOTAL_PAGES = 1200

private fun pageToMonth(page: Int): YearMonth =
    YearMonth.now().plusMonths((page - INITIAL_PAGE).toLong())

private fun monthToPage(month: YearMonth): Int =
    (INITIAL_PAGE + ChronoUnit.MONTHS.between(YearMonth.now(), month)).toInt()

private fun getWeeksForMonth(month: YearMonth): List<List<LocalDate>> {
    val firstDay = month.atDay(1)
    val daysFromSunday = if (firstDay.dayOfWeek == DayOfWeek.SUNDAY) 0L else firstDay.dayOfWeek.value.toLong()
    val gridStart = firstDay.minusDays(daysFromSunday)

    val lastDay = month.atEndOfMonth()
    val daysToSaturday = if (lastDay.dayOfWeek == DayOfWeek.SATURDAY) 0L else (6 - lastDay.dayOfWeek.value % 7).toLong()
    val gridEnd = lastDay.plusDays(daysToSaturday)

    return generateSequence(gridStart) { it.plusDays(1) }
        .takeWhile { !it.isAfter(gridEnd) }
        .toList()
        .chunked(7)
}

private fun findTodayWeekIndex(weeks: List<List<LocalDate>>, today: LocalDate): Int =
    weeks.indexOfFirst { today in it }.coerceAtLeast(0)

@Composable
fun DZamCalendar(
    selectedDate: LocalDate? = null,
    events: Map<LocalDate, List<DZamCalendarEvent>> = emptyMap(),
    calendarBodyHeight: Dp = 300.dp,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { TOTAL_PAGES }
    val coroutineScope = rememberCoroutineScope()
    val currentMonth = pageToMonth(pagerState.currentPage)
    val resolvedSelected = selectedDate ?: today

    fun defaultDateForMonth(month: YearMonth): LocalDate =
        if (YearMonth.from(today) == month) today else month.atDay(1)

    Column(modifier = Modifier.fillMaxWidth()) {
        DZamCalendarHeader(
            currentMonth = currentMonth,
            onPrevMonth = {
                val target = currentMonth.minusMonths(1)
                onDateSelected(defaultDateForMonth(target))
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNextMonth = {
                val target = currentMonth.plusMonths(1)
                onDateSelected(defaultDateForMonth(target))
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
        DZamCalendarDayOfWeekRow()
        DZamCalendarBody(
            pagerState = pagerState,
            selectedDate = resolvedSelected,
            events = events,
            today = today,
            onDateSelected = onDateSelected,
            coroutineScope = coroutineScope,
            calendarBodyHeight = calendarBodyHeight
        )
    }
}

@Composable
private fun DZamCalendarHeader(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                val direction = if (targetState > initialState) -1 else 1
                (slideInVertically { it * direction } + fadeIn()) togetherWith
                        (slideOutVertically { -it * direction } + fadeOut())
            },
            label = "monthTitle"
        ) { month ->
            Text(
                text = "${month.year}년 ${month.monthValue}월",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DZamCalendarDayOfWeekRow() {
    val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth()) {
        dayLabels.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when (index) {
                    0 -> Color.Red
                    6 -> Color.Blue
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun DZamCalendarBody(
    pagerState: PagerState,
    selectedDate: LocalDate,
    events: Map<LocalDate, List<DZamCalendarEvent>>,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    coroutineScope: CoroutineScope,
    calendarBodyHeight: Dp
) {
    val density = LocalDensity.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val dragThreshold = with(density) { 50.dp.toPx() }
    val maxDragPx = with(density) { calendarBodyHeight.toPx() }
    var isExpanded by remember { mutableStateOf(true) }
    val expansionProgress = remember { Animatable(1f) }
    val skipNextSettledPage = remember { booleanArrayOf(false) }

    LaunchedEffect(pagerState) {
        var previousPage = pagerState.settledPage
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                if (page != previousPage) {
                    previousPage = page
                    if (skipNextSettledPage[0]) {
                        skipNextSettledPage[0] = false
                    } else {
                        val month = pageToMonth(page)
                        val dateToSelect = if (YearMonth.from(today) == month) today else month.atDay(1)
                        onDateSelected(dateToSelect)
                    }
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isExpanded) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                        val newProgress = if (isExpanded) {
                            (1f + dragAccumulator / maxDragPx).coerceIn(0f, 1f)
                        } else {
                            (dragAccumulator / maxDragPx).coerceIn(0f, 1f)
                        }
                        coroutineScope.launch { expansionProgress.snapTo(newProgress) }
                    },
                    onDragEnd = {
                        val shouldCollapse = dragAccumulator < -dragThreshold && isExpanded
                        val shouldExpand = dragAccumulator > dragThreshold && !isExpanded
                        if (shouldCollapse) isExpanded = false
                        if (shouldExpand) isExpanded = true
                        coroutineScope.launch {
                            expansionProgress.animateTo(if (isExpanded) 1f else 0f, spring())
                        }
                        dragAccumulator = 0f
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            expansionProgress.animateTo(if (isExpanded) 1f else 0f, spring())
                        }
                        dragAccumulator = 0f
                    }
                )
            }
    ) { page ->
        DZamCalendarPage(
            page = page,
            selectedDate = selectedDate,
            events = events,
            today = today,
            expansionProgress = expansionProgress.value,
            onDateSelected = onDateSelected,
            onNavigateToPage = { targetPage ->
                skipNextSettledPage[0] = true
                coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
            },
            calendarBodyHeight = calendarBodyHeight
        )
    }
}

@Composable
private fun DZamCalendarPage(
    page: Int,
    selectedDate: LocalDate,
    events: Map<LocalDate, List<DZamCalendarEvent>>,
    today: LocalDate,
    expansionProgress: Float,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToPage: (Int) -> Unit,
    calendarBodyHeight: Dp
) {
    val pageMonth = remember(page) { pageToMonth(page) }
    val weeks = remember(pageMonth) { getWeeksForMonth(pageMonth) }
    val visibleWeekIndex = remember(weeks, selectedDate, today) {
        val idx = weeks.indexOfFirst { selectedDate in it }
        if (idx >= 0) idx else findTodayWeekIndex(weeks, today)
    }
    val weekHeight = calendarBodyHeight / weeks.size
    val clampedProgress = expansionProgress.coerceIn(0f, 1f)
    val containerHeight = weekHeight + (weeks.size - 1) * weekHeight * clampedProgress
    val columnOffset = weekHeight * visibleWeekIndex * (1f - clampedProgress)
    val density = LocalDensity.current

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds(),
        content = {
            weeks.forEach { week ->
                DZamCalendarWeekRow(
                    week = week,
                    pageMonth = pageMonth,
                    selectedDate = selectedDate,
                    events = events,
                    today = today,
                    onDateSelected = onDateSelected,
                    onNavigateToPage = onNavigateToPage,
                    weekHeight = weekHeight
                )
            }
        }
    ) { measurables, constraints ->
        val weekHeightPx = with(density) { weekHeight.roundToPx() }
        val containerHeightPx = with(density) { containerHeight.roundToPx() }
        val columnOffsetPx = with(density) { columnOffset.roundToPx() }
        val placeables = measurables.map { it.measure(constraints.copy(minHeight = weekHeightPx, maxHeight = weekHeightPx)) }
        layout(constraints.maxWidth, containerHeightPx) {
            var y = -columnOffsetPx
            placeables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
private fun DZamCalendarWeekRow(
    week: List<LocalDate>,
    pageMonth: YearMonth,
    selectedDate: LocalDate,
    events: Map<LocalDate, List<DZamCalendarEvent>>,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNavigateToPage: (Int) -> Unit,
    weekHeight: Dp
) {
    Row(modifier = Modifier.fillMaxWidth().height(weekHeight)) {
        week.forEachIndexed { dayIndex, date ->
            val isOtherMonth = YearMonth.from(date) != pageMonth
            val isToday = date == today
            val isSelected = date == selectedDate
            val dayEvents = events[date] ?: emptyList()

            val onClick: () -> Unit = {
                if (isOtherMonth) onNavigateToPage(monthToPage(YearMonth.from(date)))
                onDateSelected(date)
            }

            DZamCalendarDateCell(
                date = date,
                dayIndex = dayIndex,
                isOtherMonth = isOtherMonth,
                isToday = isToday,
                isSelected = isSelected,
                events = dayEvents,
                onClick = onClick,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun DZamCalendarDateCell(
    date: LocalDate,
    dayIndex: Int,
    isOtherMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<DZamCalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateTextColor = when {
        dayIndex == 0 -> Color.Red
        dayIndex == 6 -> Color.Blue
        else -> MaterialTheme.colorScheme.onSurface
    }
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .alpha(if (isOtherMonth) 0.3f else 1f)
            .then(
                if (isSelected) Modifier.border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (isToday) Modifier
                            .clip(CircleShape)
                            .background(primaryContainerColor)
                        else Modifier
                    )
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = dateTextColor,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            if (events.isNotEmpty()) {
                DZamCalendarEventIndicators(
                    events = events,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DZamCalendarEventIndicators(
    events: List<DZamCalendarEvent>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val chipHeightDp = 16.dp
        val maxChips = (maxHeight / chipHeightDp).toInt().coerceAtLeast(0)

        if (events.size <= maxChips.coerceAtLeast(1)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                events.forEach { event ->
                    Surface(
                        color = event.backgroundColor,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chipHeightDp)
                    ) {
                        Text(
                            text = event.text,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                events.forEach { event ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(event.backgroundColor)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DZamCalendar - 기본")
@Composable
private fun DZamCalendarPreview() {
    val today = LocalDate.now()
    val sampleEvents = mapOf(
        today to listOf(
            DZamCalendarEvent("벤치프레스", Color(0xFF4CAF50)),
            DZamCalendarEvent("스쿼트", Color(0xFF2196F3))
        ),
        today.minusDays(2) to listOf(
            DZamCalendarEvent("데드리프트", Color(0xFFF44336))
        ),
        today.plusDays(1) to listOf(
            DZamCalendarEvent("오버헤드프레스", Color(0xFFFF9800)),
            DZamCalendarEvent("풀업", Color(0xFF9C27B0)),
            DZamCalendarEvent("딥스", Color(0xFF009688))
        ),
        today.plusDays(3) to listOf(
            DZamCalendarEvent("런지", Color(0xFFE91E63))
        )
    )

    FitzamTheme {
        Surface {
            DZamCalendar(
                selectedDate = today,
                events = sampleEvents
            )
        }
    }
}
