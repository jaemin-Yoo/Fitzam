package com.jaemin.fitzam.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object CalendarUtils {

    private const val START_YEAR = 2000
    private const val END_YEAR = 2500

    // 월,화,수,목,금,토,일 -> 일,월,화,수,목,금,토
    val dayOfWeek = listOf(DayOfWeek.entries.last()) + DayOfWeek.entries.dropLast(1)

    val startMonth: YearMonth = YearMonth.of(START_YEAR, 1)
    val endMonth: YearMonth = YearMonth.of(END_YEAR, 12)
    val totalMonths: Int = ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1

    data class CalendarDay(
        val date: LocalDate,
        val isInMonth: Boolean,
    )

    // Calendar grid dates for a given month, including previous/next month fillers.
    fun getCalendarMonthDays(yearMonth: YearMonth): List<CalendarDay> {
        val firstDay = yearMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
        val totalDays = yearMonth.lengthOfMonth()

        val result = mutableListOf<CalendarDay>()

        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLastDay = prevMonth.lengthOfMonth()
        for (d in prevMonthLastDay - firstDayOfWeek + 1..prevMonthLastDay) {
            if (firstDayOfWeek > 0) {
                result.add(CalendarDay(prevMonth.atDay(d), isInMonth = false))
            }
        }

        for (d in 1..totalDays) {
            result.add(CalendarDay(yearMonth.atDay(d), isInMonth = true))
        }

        val nextMonth = yearMonth.plusMonths(1)
        val trailingCount = (7 - result.size % 7) % 7
        for (d in 1..trailingCount) {
            result.add(CalendarDay(nextMonth.atDay(d), isInMonth = false))
        }

        return result
    }
}
