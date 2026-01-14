package com.jaemin.fitzam.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.ui.common.CalendarDayItem
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.common.FitzamBrandTopAppBar
import com.jaemin.fitzam.ui.common.FitzamCalendar
import com.jaemin.fitzam.ui.common.FitzamCalendarDayList
import com.jaemin.fitzam.ui.common.FitzamCalendarState
import com.jaemin.fitzam.ui.common.FitzamFloatingActionButton
import com.jaemin.fitzam.ui.common.rememberFitzamCalendarState
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onAddOrEditWorkout: (LocalDate) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val calendarState = rememberFitzamCalendarState()

    // 캘린더 월 이동 시 운동 기록 가져오기
    LaunchedEffect(calendarState.displayedYearMonth) {
        viewModel.loadWorkoutsForYearMonth(calendarState.displayedYearMonth)
    }

    HomeScreen(
        workouts = workouts,
        calendarState = calendarState,
        onAddOrEditWorkout = onAddOrEditWorkout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<Workout>,
    calendarState: FitzamCalendarState,
    onAddOrEditWorkout: (LocalDate) -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamBrandTopAppBar(
                logoRes = R.drawable.fitzam_logo
            )
        },
        floatingActionButton = {
            FitzamFloatingActionButton(
                icon = if (workouts.any { it.date == calendarState.selectedDate }) {
                    ImageVector.vectorResource(R.drawable.ic_edit)
                } else {
                    ImageVector.vectorResource(R.drawable.ic_plus)
                },
                onClick = { onAddOrEditWorkout(calendarState.selectedDate) },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp,
                )
                .verticalScroll(rememberScrollState()),
        ) {
            FitzamCalendar(
                state = calendarState,
                modifier = Modifier.padding(vertical = 8.dp),
                dayContent = { date ->
                    workouts.forEach { workout ->
                        if (date == workout.date) {
                            FitzamCalendarDayList(
                                itemList = workout.exerciseCategories.map { category ->
                                    CalendarDayItem(
                                        text = category.name,
                                        color = Color(category.colorHex),
                                    )
                                },
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "${calendarState.selectedDate.monthValue}월 ${calendarState.selectedDate.dayOfMonth}일 (${
                    calendarState.selectedDate.dayOfWeek.getDisplayName(
                        TextStyle.NARROW,
                        Locale.KOREAN
                    )
                })",
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val workoutOfSelectedDate =
                    workouts.firstOrNull { it.date == calendarState.selectedDate }
                workoutOfSelectedDate?.exerciseCategories?.forEach { category ->
                    ExerciseCategoryTag(
                        name = category.name,
                        borderColor = Color(category.colorHex),
                    )
                }
            }
            Spacer(Modifier.height(88.dp))
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    FitzamTheme {
        val calendarState = rememberFitzamCalendarState()
        HomeScreen(
            calendarState = calendarState,
            workouts = emptyList(),
            onAddOrEditWorkout = {},
        )
    }
}
