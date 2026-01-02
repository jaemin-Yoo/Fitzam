package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.CalendarUtils.dayOfWeek
import com.jaemin.fitzam.ui.util.CalendarUtils.getCalendarMonthDays
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val MAX_CELL_ITEM_COUNT = 3

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
    val currentDate = LocalDate.now()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FitzamCalendarHeader(
                selectedDate = selectedDate,
                onPreviousMonthClick = {
                    // 이전 달로 이동 시 이전 달 1일 선택
                    onDateSelected(
                        selectedDate.minusMonths(1).withDayOfMonth(1)
                    )
                },
                onNextMonthClick = {
                    // 다음 달로 이동 시 다음 달 1일 선택
                    onDateSelected(
                        selectedDate.plusMonths(1).withDayOfMonth(1)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            FitzamCalendarContent(
                selectedDate = selectedDate,
                currentDate = currentDate,
                onDateSelected = onDateSelected,
                dateContent = { dateContent(it) }
            )
        }
    }
}

@Composable
fun FitzamCalendarCellList(
    itemList: List<CalendarCellItem>,
) {
    if (itemList.size <= MAX_CELL_ITEM_COUNT) {
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
    selectedDate: LocalDate,
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
            text = "${selectedDate.year}년 ${selectedDate.monthValue}월",
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
    selectedDate: LocalDate,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dateContent: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    val monthDays = remember(selectedDate) {
        getCalendarMonthDays(YearMonth.from(selectedDate))
    }

    Column(modifier = modifier) {
        // 요일
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(monthDays.size) { i ->
                val dayDate = monthDays[i]

                FitzamCalendarCell(
                    dayDate = dayDate,
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
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(LocalDate) -> Unit = {},
) {
    Column(
        modifier = modifier
            .height(80.dp)
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
