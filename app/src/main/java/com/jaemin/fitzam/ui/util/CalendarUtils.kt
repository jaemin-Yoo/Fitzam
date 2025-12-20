package com.jaemin.fitzam.ui.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

object CalendarUtils {

    // 월,화,수,목,금,토,일 -> 일,월,화,수,목,금,토
    val dayOfWeek = listOf(DayOfWeek.entries.last()) + DayOfWeek.entries.dropLast(1)

    // 달력 년월별 날짜 리스트
    fun getCalendarMonthDays(yearMonth: YearMonth): List<LocalDate?> {
        val firstDay = yearMonth.atDay(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7
        val totalDays = yearMonth.lengthOfMonth()

        val result = mutableListOf<LocalDate?>()

        repeat(firstDayOfWeek) { result.add(null) }

        for (d in 1..totalDays) {
            result.add(yearMonth.atDay(d))
        }

        while (result.size % 7 != 0) {
            result.add(null)
        }

        return result
    }
}