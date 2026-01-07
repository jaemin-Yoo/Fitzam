package com.jaemin.fitzam.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object CalendarUtils {

    /**
     * 캘린더 시작년월: 2000년 1월
     * 캘린더 종료년월: 2500년 12월
     */
    val startMonth: YearMonth = YearMonth.of(2000, 1)
    val endMonth: YearMonth = YearMonth.of(2500, 12)
    val totalMonths: Int = ChronoUnit.MONTHS.between(startMonth, endMonth).toInt() + 1

    // 월 화,수,목,금,토,일 -> 일,월,화,수,목,금,토
    val dayOfWeek = listOf(DayOfWeek.entries.last()) + DayOfWeek.entries.dropLast(1)

    data class CalendarDay(
        val date: LocalDate,
        val isInMonth: Boolean,
    )

    /**
     * 월별 날짜 리스트
     * 전달 마지막 주, 다음 달 첫 주 일부 날짜 포함
     */
    fun getCalendarMonthDays(yearMonth: YearMonth): List<CalendarDay> {
        val monthDays = mutableListOf<CalendarDay>()

        val firstDay = yearMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLastDay = prevMonth.lengthOfMonth()
        for (d in prevMonthLastDay - firstDayOfWeek + 1..prevMonthLastDay) {
            val day = CalendarDay(
                date = prevMonth.atDay(d),
                isInMonth = false,
            )
            monthDays.add(day)
        }

        val lastDay = yearMonth.lengthOfMonth()
        for (d in 1..lastDay) {
            val day = CalendarDay(
                date = yearMonth.atDay(d),
                isInMonth = true,
            )
            monthDays.add(day)
        }

        val nextMonth = yearMonth.plusMonths(1)
        val trailingCount = (7 - monthDays.size % 7) % 7
        for (d in 1..trailingCount) {
            val day = CalendarDay(
                date = nextMonth.atDay(d),
                isInMonth = false,
            )
            monthDays.add(day)
        }

        return monthDays
    }
}