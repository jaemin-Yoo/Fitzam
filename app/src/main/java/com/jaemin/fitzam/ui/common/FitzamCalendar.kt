package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.CalendarUtils.dayOfWeek
import com.jaemin.fitzam.ui.util.CalendarUtils.getCalendarMonthDays
import com.jaemin.fitzam.ui.util.CalendarUtils.startMonth
import com.jaemin.fitzam.ui.util.CalendarUtils.totalMonths
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun rememberFitzamCalendarState(
    initialSelectedDate: LocalDate = LocalDate.now(),
    initialDisplayedYearMonth: YearMonth = YearMonth.now(),
): FitzamCalendarState {
    return rememberSaveable(saver = FitzamCalendarState.Saver) {
        FitzamCalendarState(
            initialSelectedDate = initialSelectedDate,
            initialDisplayedYearMonth = initialDisplayedYearMonth,
        )
    }
}

@Stable
class FitzamCalendarState(
    initialSelectedDate: LocalDate,
    initialDisplayedYearMonth: YearMonth,
) {
    var selectedDate by mutableStateOf(initialSelectedDate)
        internal set

    var displayedYearMonth by mutableStateOf(initialDisplayedYearMonth)
        internal set

    companion object {
        val Saver = listSaver<FitzamCalendarState, String>(
            save = {
                listOf(
                    it.selectedDate.toString(),
                    it.displayedYearMonth.toString()
                )
            },
            restore = { restored ->
                val selectedDate = LocalDate.parse(restored[0])
                val displayedYearMonth = YearMonth.parse(restored[1])
                FitzamCalendarState(
                    initialSelectedDate = selectedDate,
                    initialDisplayedYearMonth = displayedYearMonth,
                )
            },
        )
    }
}

@Composable
fun FitzamCalendar(
    state: FitzamCalendarState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    calendarHeight: Dp = 400.dp,
    dayContent: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    val displayedPage = yearMonthToPage(state.displayedYearMonth)
    val pagerState = rememberPagerState(
        initialPage = displayedPage,
        pageCount = { totalMonths },
    )

    /**
     * 날짜 선택으로 캘린더 스와이프
     */
    val selectedYearMonth = YearMonth.from(state.selectedDate)
    LaunchedEffect(selectedYearMonth) {
        val selectedPage = yearMonthToPage(selectedYearMonth)
        if (selectedPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(selectedPage)
        }
    }

    /**
     * 캘린더 스와이프 (월 이동)
     * 날짜 선택으로 스와이프가 된 경우가 아니면 1일 선택
     */
    LaunchedEffect(pagerState.currentPage) {
        val currentYearMonth = pageToYearMonth(pagerState.currentPage)
        state.displayedYearMonth = currentYearMonth

        if (selectedYearMonth != currentYearMonth) {
            val newSelectedDate = currentYearMonth.atDay(1)
            state.selectedDate = newSelectedDate
        }
    }

    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = modifier,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CalendarHeader(
                yearMonth = state.displayedYearMonth,
                onPreviousMonthClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNextMonthClick = {
                    if (pagerState.currentPage < totalMonths - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))

            DaysOfWeekHeader(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val yearMonth = pageToYearMonth(page)
                CalendarContent(
                    calendarHeight = calendarHeight,
                    yearMonth = yearMonth,
                    selectedDate = state.selectedDate,
                    onDateSelected = { state.selectedDate = it },
                    dayContent = dayContent,
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                contentDescription = "이전 달로 이동",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onNextMonthClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = "다음 달로 이동",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
    ) {
        dayOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = when (day) {
                    DayOfWeek.SUNDAY -> Color(0xFFFB2C36)
                    DayOfWeek.SATURDAY -> Color(0xFF2B7FFF)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CalendarContent(
    calendarHeight: Dp,
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dayContent: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    val monthDays = remember(yearMonth) { getCalendarMonthDays(yearMonth) }

    // Week 개수에 따라 날짜 셀 크기 조정
    val rowCount = (monthDays.size / 7).coerceIn(MIN_WEEK_COUNT, MAX_WEEK_COUNT)
    val cellHeight =
        (calendarHeight - GRID_SPACING * (rowCount - 1) - GRID_BORDER_WIDTH + 1.dp) / rowCount

    val currentDate = remember { LocalDate.now() }
    LazyVerticalGrid(
        modifier = modifier.height(calendarHeight),
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        userScrollEnabled = false,
    ) {
        items(monthDays.size) { i ->
            val day = monthDays[i]

            CalendarDay(
                cellHeight = cellHeight,
                dayDate = day.date,
                isInMonth = day.isInMonth,
                isSelected = day.date == selectedDate,
                isToday = day.date == currentDate,
                onClick = { onDateSelected(it) },
                content = { dayContent(it) }
            )
        }
    }
}

@Composable
private fun CalendarDay(
    cellHeight: Dp,
    dayDate: LocalDate,
    isInMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    Column(
        modifier = modifier
            .height(cellHeight)
            .alpha(if (isInMonth) 1f else 0.2f)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = GRID_BORDER_WIDTH,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable { onClick(dayDate) },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayDate.dayOfMonth.toString(),
            modifier = Modifier
                .padding(
                    start = 2.dp,
                    end = 2.dp,
                    top = 2.dp,
                )
                .then(
                    if (isToday) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                        )
                    } else {
                        Modifier
                    }
                )
                .fillMaxWidth(),
            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                content(dayDate)
            }
        }
    }
}

private fun yearMonthToPage(yearMonth: YearMonth): Int {
    val rawOffset = ChronoUnit.MONTHS.between(startMonth, yearMonth).toInt()
    return rawOffset.coerceIn(0, totalMonths - 1)
}

private fun pageToYearMonth(page: Int): YearMonth {
    val safePage = page.coerceIn(0, totalMonths - 1)
    return startMonth.plusMonths(safePage.toLong())
}

@Composable
fun FitzamCalendarDayList(
    itemList: List<CalendarDayItem>,
) {
    if (itemList.size <= MAX_CELL_LIST_ITEM_COUNT) {
        // 아이템이 3개 이하인 경우, 리스트 형태로 표시
        itemList.forEach { item ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .padding(vertical = 2.dp)
                        .fillMaxHeight()
                        .background(
                            color = item.color,
                            shape = RoundedCornerShape(16.dp),
                        ),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = item.text,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        // "아이템이 4개 이상인 경우, 원 형태로 표시"
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(2.dp),
        ) {
            items(
                count = itemList.size,
                key = { index -> itemList[index].text }
            ) { index ->
                val item = itemList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(item.color),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FitzamCalendarPreview() {
    FitzamTheme {
        Box(
            Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            val calendarState = rememberFitzamCalendarState()
            FitzamCalendar(
                state = calendarState,
                dayContent = { date ->
                    if (date == LocalDate.now()) {
                        FitzamCalendarDayList(
                            itemList = listOf(
                                CalendarDayItem(
                                    text = "가슴",
                                    color = Color.Blue,
                                ),
                                CalendarDayItem(
                                    text = "유산소",
                                    color = Color.Green,
                                ),
                            )
                        )
                    } else if (date == LocalDate.now().plusDays(1)) {
                        FitzamCalendarDayList(
                            itemList = listOf(
                                CalendarDayItem(
                                    text = "가슴",
                                    color = Color.Blue,
                                ),
                                CalendarDayItem(
                                    text = "유산소",
                                    color = Color.Green,
                                ),
                                CalendarDayItem(
                                    text = "어깨",
                                    color = Color.Black,
                                ),
                                CalendarDayItem(
                                    text = "복근",
                                    color = Color.Red,
                                ),
                                CalendarDayItem(
                                    text = "삼두",
                                    color = Color.Gray,
                                ),
                            )
                        )
                    }
                }
            )
        }
    }
}

private const val MAX_CELL_LIST_ITEM_COUNT = 3
private const val MIN_WEEK_COUNT = 4
private const val MAX_WEEK_COUNT = 6
private val GRID_SPACING = 8.dp
private val GRID_BORDER_WIDTH = 1.dp

data class CalendarDayItem(
    val text: String,
    val color: Color,
)
