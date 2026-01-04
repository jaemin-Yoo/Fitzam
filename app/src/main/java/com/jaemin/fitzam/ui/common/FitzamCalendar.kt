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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.CalendarUtils.dayOfWeek
import com.jaemin.fitzam.ui.util.CalendarUtils.getCalendarMonthDays
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val MAX_CELL_LIST_ITEM_COUNT = 3
private const val START_YEAR = 2000
private const val END_YEAR = 2500
private const val MIN_WEEK_COUNT = 4
private const val MAX_WEEK_COUNT = 6
private val GRID_SPACING = 8.dp
private val GRID_BORDER_PADDING = 2.dp
private val CALENDAR_HEIGHT = 400.dp

data class CalendarCellItem(
    val text: String,
    val color: Color,
)

@Composable
fun FitzamCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dateContent: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val currentDate = LocalDate.now()
    val startMonth = remember { YearMonth.of(START_YEAR, 1) }
    val endMonth = remember { YearMonth.of(END_YEAR, 12) }
    val totalMonths = remember { ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1 }
    val selectedMonth = YearMonth.from(selectedDate)
    val selectedMonthPage = remember(selectedMonth) {
        val rawOffset = ChronoUnit.MONTHS.between(startMonth, selectedMonth).toInt()
        rawOffset.coerceIn(0, totalMonths - 1)
    }
    val suppressAutoSelect = remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = selectedMonthPage,
        pageCount = { totalMonths },
    )

    // 선택된 날짜가 현재 월이 아니면 월 이동
    LaunchedEffect(selectedMonthPage) {
        if (pagerState.currentPage != selectedMonthPage) {
            suppressAutoSelect.value = true
            pagerState.scrollToPage(selectedMonthPage)
        }
    }

    // 월 이동 시 해당 월 1일로 선택
    LaunchedEffect(pagerState.currentPage) {
        if (suppressAutoSelect.value) {
            // 날짜를 선택한 경우 1일 선택 안함
            suppressAutoSelect.value = false
            return@LaunchedEffect
        }

        val pageMonth = startMonth.plusMonths(pagerState.currentPage.toLong())
        val newSelectedDate = pageMonth.atDay(1)
        onDateSelected(newSelectedDate)
    }

    Surface(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            FitzamCalendarHeader(
                yearMonth = YearMonth.from(selectedDate),
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val month = startMonth.plusMonths(page.toLong())
                FitzamCalendarContent(
                    monthDate = month.atDay(1),
                    selectedDate = selectedDate,
                    currentDate = currentDate,
                    onDateSelected = onDateSelected,
                    dateContent = { dateContent(it) }
                )
            }
        }
    }
}

@Composable
fun FitzamCalendarCellList(
    itemList: List<CalendarCellItem>,
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

@Composable
private fun FitzamCalendarHeader(
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
private fun FitzamCalendarContent(
    monthDate: LocalDate,
    selectedDate: LocalDate,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dateContent: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    val monthDays = remember(monthDate) {
        getCalendarMonthDays(YearMonth.from(monthDate))
    }

    // 캘린더 날짜 고정 크기 설정
    val rowCount = (monthDays.size / 7).coerceIn(MIN_WEEK_COUNT, MAX_WEEK_COUNT)
    val cellHeight = (CALENDAR_HEIGHT - GRID_SPACING * (rowCount - 1) - GRID_BORDER_PADDING) / rowCount

    Column(modifier = modifier) {
        // 요일
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        Spacer(modifier = Modifier.height(16.dp))

        // 날짜
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(CALENDAR_HEIGHT),
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(GRID_SPACING),
            horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
            userScrollEnabled = false,
        ) {
            items(monthDays.size) { i ->
                val dayDate = monthDays[i]

                FitzamCalendarCell(
                    dayDate = dayDate,
                    cellHeight = cellHeight,
                    isSelected = dayDate == selectedDate,
                    isToday = dayDate == currentDate,
                    onClick = { onDateSelected(it) },
                    content = { dateContent(it) }
                )
            }
        }
    }
}

@Composable
private fun FitzamCalendarCell(
    dayDate: LocalDate?,
    cellHeight: Dp,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    Column(
        modifier = modifier
            .height(cellHeight)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(enabled = dayDate != null) { dayDate?.let(onClick) },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayDate?.dayOfMonth?.toString() ?: "",
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
        if (dayDate != null) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                    content(dayDate)
                }
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
            FitzamCalendar(
                selectedDate = LocalDate.now(),
                onDateSelected = {},
                dateContent = { date ->
                    if (date == LocalDate.now()) {
                        FitzamCalendarCellList(
                            itemList = listOf(
                                CalendarCellItem(
                                    text = "가슴",
                                    color = Color.Blue,
                                ),
                                CalendarCellItem(
                                    text = "유산소",
                                    color = Color.Green,
                                ),
                            )
                        )
                    } else if (date == LocalDate.now().plusDays(1)) {
                        FitzamCalendarCellList(
                            itemList = listOf(
                                CalendarCellItem(
                                    text = "가슴",
                                    color = Color.Blue,
                                ),
                                CalendarCellItem(
                                    text = "유산소",
                                    color = Color.Green,
                                ),
                                CalendarCellItem(
                                    text = "어깨",
                                    color = Color.Black,
                                ),
                                CalendarCellItem(
                                    text = "복근",
                                    color = Color.Red,
                                ),
                                CalendarCellItem(
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
