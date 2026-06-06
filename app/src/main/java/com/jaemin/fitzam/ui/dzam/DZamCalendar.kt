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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
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

@Stable
class DZamCalendarState(
    initialSelectedDate: LocalDate,
    initialDisplayedYearMonth: YearMonth,
) {
    var selectedDate by mutableStateOf(initialSelectedDate)
        internal set

    var displayedYearMonth by mutableStateOf(initialDisplayedYearMonth)
        internal set

    companion object {
        val Saver = listSaver<DZamCalendarState, String>(
            save = { listOf(it.selectedDate.toString(), it.displayedYearMonth.toString()) },
            restore = { restored ->
                DZamCalendarState(
                    initialSelectedDate = LocalDate.parse(restored[0]),
                    initialDisplayedYearMonth = YearMonth.parse(restored[1]),
                )
            }
        )
    }
}

@Composable
fun rememberDZamCalendarState(
    initialSelectedDate: LocalDate = LocalDate.now(),
    initialDisplayedYearMonth: YearMonth = YearMonth.now(),
): DZamCalendarState {
    return rememberSaveable(saver = DZamCalendarState.Saver) {
        DZamCalendarState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedYearMonth = initialDisplayedYearMonth,
        )
    }
}

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
    state: DZamCalendarState,
    events: Map<LocalDate, List<DZamCalendarEvent>> = emptyMap(),
    calendarBodyHeight: Dp = 400.dp,
) {
    val today = remember { LocalDate.now() }
    val pagerState = rememberPagerState(initialPage = monthToPage(state.displayedYearMonth)) { TOTAL_PAGES }
    val coroutineScope = rememberCoroutineScope()
    val currentMonth = pageToMonth(pagerState.currentPage)

    val selectedYearMonth = YearMonth.from(state.selectedDate)
    LaunchedEffect(selectedYearMonth) {
        val targetPage = monthToPage(selectedYearMonth)
        if (targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    fun defaultDateForMonth(month: YearMonth): LocalDate =
        if (YearMonth.from(today) == month) today else month.atDay(1)

    fun onDateSelected(date: LocalDate) {
        state.selectedDate = date
        state.displayedYearMonth = YearMonth.from(date)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(modifier = Modifier.height(20.dp))
            DZamCalendarDayOfWeekRow()
            DZamCalendarBody(
                pagerState = pagerState,
                selectedDate = state.selectedDate,
                events = events,
                today = today,
                onDateSelected = ::onDateSelected,
                coroutineScope = coroutineScope,
                calendarBodyHeight = calendarBodyHeight
            )
        }
    }
}

@Composable
private fun DZamCalendarHeader(
    currentMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                style = MaterialTheme.typography.titleMedium
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
                style = MaterialTheme.typography.labelMedium,
                color = when (index) {
                    0 -> Color(0xFFFB2C36)
                    6 -> Color(0xFF2B7FFF)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
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
    val spacingCount = weeks.size - 1
    val weekHeight = (calendarBodyHeight - DZAM_GRID_SPACING * spacingCount) / weeks.size
    val clampedProgress = expansionProgress.coerceIn(0f, 1f)
    val containerHeight = weekHeight + spacingCount * (weekHeight + DZAM_GRID_SPACING) * clampedProgress
    val columnOffset = (weekHeight + DZAM_GRID_SPACING) * visibleWeekIndex * (1f - clampedProgress)
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
        val spacingPx = with(density) { DZAM_GRID_SPACING.roundToPx() }
        val placeables = measurables.map { it.measure(constraints.copy(minHeight = weekHeightPx, maxHeight = weekHeightPx)) }
        layout(constraints.maxWidth, containerHeightPx) {
            var y = -columnOffsetPx
            placeables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height + spacingPx
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
    Row(
        modifier = Modifier.fillMaxWidth().height(weekHeight),
        horizontalArrangement = Arrangement.spacedBy(DZAM_GRID_SPACING)
    ) {
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
        dayIndex == 0 -> Color(0xFFFB2C36)
        dayIndex == 6 -> Color(0xFF2B7FFF)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .alpha(if (isOtherMonth) 0.3f else 1f)
            .then(
                if (isSelected) Modifier.border(1.dp, primaryColor, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier
                    .padding(start = 2.dp, end = 2.dp, top = 2.dp)
                    .then(
                        if (isToday) Modifier.background(
                            color = primaryContainerColor,
                            shape = RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else dateTextColor,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            if (events.isNotEmpty()) {
                DZamCalendarEventIndicators(
                    events = events,
                    modifier = Modifier.fillMaxWidth()
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
    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        val listPlaceable = subcompose("list") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                events.forEach { event ->
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .padding(vertical = 2.dp)
                                .fillMaxHeight()
                                .background(
                                    color = event.backgroundColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = event.text,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }.firstOrNull()?.measure(looseConstraints)

        if (listPlaceable != null && listPlaceable.height <= constraints.maxHeight) {
            layout(listPlaceable.width, listPlaceable.height) {
                listPlaceable.place(0, 0)
            }
        } else {
            val dotsPlaceable = subcompose("dots") {
                val dotSizePx = (constraints.maxWidth - with(density) { 12.dp.roundToPx() }) / 3
                val dotSize = with(density) { dotSizePx.toDp() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    events.chunked(3).forEach { rowEvents ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowEvents.forEach { event ->
                                Box(
                                    modifier = Modifier
                                        .size(dotSize)
                                        .clip(CircleShape)
                                        .background(event.backgroundColor)
                                )
                            }
                        }
                    }
                }
            }.firstOrNull()?.measure(looseConstraints)

            if (dotsPlaceable == null) {
                layout(constraints.minWidth, constraints.minHeight) {}
            } else {
                layout(dotsPlaceable.width, dotsPlaceable.height) {
                    dotsPlaceable.place(0, 0)
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
                state = rememberDZamCalendarState(),
                events = sampleEvents
            )
        }
    }
}

private val DZAM_GRID_SPACING = 8.dp
