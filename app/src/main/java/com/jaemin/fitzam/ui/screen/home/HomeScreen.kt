package com.jaemin.fitzam.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.ExerciseRecordSchema
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutExercise
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.WorkoutSet
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.CalendarDayItem
import com.jaemin.fitzam.ui.dzam.FitzamCalendar
import com.jaemin.fitzam.ui.dzam.FitzamCalendarDayList
import com.jaemin.fitzam.ui.dzam.FitzamCalendarState
import com.jaemin.fitzam.ui.dzam.FitzamFloatingActionButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.dzam.rememberFitzamCalendarState
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.drawableResIdByName
import com.jaemin.fitzam.ui.util.formatDurationInMinutesAndSeconds
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onAddOrEditWorkout: (LocalDate) -> Unit,
    onWorkoutDetailClick: (LocalDate, Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val selectedDateWorkoutExercises by viewModel.selectedDateWorkoutExercises.collectAsStateWithLifecycle()
    val calendarState = rememberFitzamCalendarState()

    LaunchedEffect(calendarState.displayedYearMonth) {
        viewModel.loadWorkoutsForYearMonth(calendarState.displayedYearMonth)
    }

    LaunchedEffect(calendarState.selectedDate) {
        viewModel.onSelectedDateChanged(calendarState.selectedDate)
    }

    HomeScreen(
        workouts = workouts,
        selectedDateWorkoutExercises = selectedDateWorkoutExercises,
        calendarState = calendarState,
        onAddOrEditWorkout = onAddOrEditWorkout,
        onSettingsClick = onSettingsClick,
        onWorkoutExerciseClick = { exerciseId ->
            onWorkoutDetailClick(calendarState.selectedDate, exerciseId)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<Workout>,
    selectedDateWorkoutExercises: List<WorkoutExercise>,
    calendarState: FitzamCalendarState,
    onAddOrEditWorkout: (LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    onWorkoutExerciseClick: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = null,
                navigation = TopAppBarItem(
                    icon = IconSource.Drawable(R.drawable.fitzam_logo),
                    contentDescription = "로고 이미지",
                    iconTint = Color.Unspecified,
                    onClick = {},
                ),
                actions = listOf(
                    TopAppBarItem(
                        icon = IconSource.Vector(ImageVector.vectorResource(R.drawable.ic_settings)),
                        contentDescription = "설정",
                        onClick = onSettingsClick,
                    ),
                ),
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
        },
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
                },
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = "${calendarState.selectedDate.monthValue}월 ${calendarState.selectedDate.dayOfMonth}일 (${
                    calendarState.selectedDate.dayOfWeek.getDisplayName(
                        TextStyle.NARROW,
                        Locale.KOREAN,
                    )
                })",
                modifier = Modifier.padding(horizontal = 8.dp),
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

            if (selectedDateWorkoutExercises.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    selectedDateWorkoutExercises.forEach { workoutExercise ->
                        HomeWorkoutExerciseCard(
                            workoutExercise = workoutExercise,
                            onClick = { onWorkoutExerciseClick(workoutExercise.exercise.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
private fun HomeWorkoutExerciseCard(
    workoutExercise: WorkoutExercise,
    onClick: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(drawableResIdByName(workoutExercise.exercise.imageName)),
                    contentDescription = workoutExercise.exercise.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.size(8.dp))

                Column {
                    Text(
                        text = workoutExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExerciseCategoryTag(
                        name = workoutExercise.exercise.category.name,
                        borderColor = Color(workoutExercise.exercise.category.colorHex),
                    )
                }
            }

            if (workoutExercise.sets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HomeWorkoutSetTable(
                    recordSchema = workoutExercise.exercise.recordSchema,
                    sets = workoutExercise.sets,
                )
            }
        }
    }
}

@Composable
internal fun HomeWorkoutSetTable(
    recordSchema: ExerciseRecordSchema,
    sets: List<WorkoutSet>,
) {
    val firstHeader = when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> "무게(KG)"
        ExerciseRecordSchema.DISTANCE_DURATION -> "거리(KM)"
    }
    val secondHeader = when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> "횟수"
        ExerciseRecordSchema.DISTANCE_DURATION -> "시간(초)"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            HomeTableHeaderCell(text = "세트", modifier = Modifier.weight(1f))
            HomeTableHeaderCell(text = firstHeader, modifier = Modifier.weight(1f))
            HomeTableHeaderCell(text = secondHeader, modifier = Modifier.weight(1f))
        }

        sets.forEach { set ->
            val firstMetric = when (recordSchema) {
                ExerciseRecordSchema.WEIGHT_REPS -> set.metrics[WorkoutMetricType.WEIGHT_KG] ?: 0.0
                ExerciseRecordSchema.DISTANCE_DURATION -> set.metrics[WorkoutMetricType.DISTANCE_KM] ?: 0.0
            }
            val secondMetric = when (recordSchema) {
                ExerciseRecordSchema.WEIGHT_REPS -> set.metrics[WorkoutMetricType.REPS] ?: 0.0
                ExerciseRecordSchema.DISTANCE_DURATION -> set.metrics[WorkoutMetricType.DURATION_SEC] ?: 0.0
            }
            val secondMetricText = when (recordSchema) {
                ExerciseRecordSchema.WEIGHT_REPS -> formatMetricValue(secondMetric)
                ExerciseRecordSchema.DISTANCE_DURATION -> {
                    formatDurationInMinutesAndSeconds(secondMetric.toInt())
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                HomeTableValueCell(text = set.index.toString(), modifier = Modifier.weight(1f))
                HomeTableValueCell(text = formatMetricValue(firstMetric), modifier = Modifier.weight(1f))
                HomeTableValueCell(text = secondMetricText, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun HomeTableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
internal fun HomeTableValueCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

internal fun formatMetricValue(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    val previewDate = LocalDate.now()
    val chest = ExerciseCategory(
        id = 0,
        name = "가슴",
        imageName = "img_chest",
        colorHex = 0xFF1D4ED8,
        colorDarkHex = 0xFF2563EB,
    )
    val shoulder = ExerciseCategory(
        id = 1,
        name = "어깨",
        imageName = "img_shoulder",
        colorHex = 0xFF950AFF,
        colorDarkHex = 0xFF950AFF,
    )

    val workouts = listOf(
        Workout(
            date = previewDate.minusDays(2),
            exerciseCategories = listOf(
                ExerciseCategory(
                    id = 1,
                    name = "등",
                    imageName = "img_back",
                    colorHex = 0xFF0891B2,
                    colorDarkHex = 0xFF14B8A6,
                ),
                ExerciseCategory(
                    id = 2,
                    name = "어깨",
                    imageName = "img_shoulder",
                    colorHex = 0xFF15803D,
                    colorDarkHex = 0xFF22C55E,
                ),
            ),
        ),
        Workout(
            date = previewDate,
            exerciseCategories = listOf(chest, shoulder),
        ),
    )

    val selectedDateWorkoutExercises = listOf(
        WorkoutExercise(
            id = 1,
            exercise = Exercise(
                id = 1,
                name = "숄더 프레스 (바벨)",
                category = chest,
                imageName = chest.imageName,
            ),
            sets = listOf(
                WorkoutSet(
                    index = 1,
                    metrics = mapOf(
                        WorkoutMetricType.WEIGHT_KG to 80.0,
                        WorkoutMetricType.REPS to 10.0,
                    ),
                ),
                WorkoutSet(
                    index = 2,
                    metrics = mapOf(
                        WorkoutMetricType.WEIGHT_KG to 85.0,
                        WorkoutMetricType.REPS to 10.0,
                    ),
                ),
                WorkoutSet(
                    index = 3,
                    metrics = mapOf(
                        WorkoutMetricType.WEIGHT_KG to 90.0,
                        WorkoutMetricType.REPS to 8.0,
                    ),
                ),
            ),
        ),
        WorkoutExercise(
            id = 2,
            exercise = Exercise(
                id = 2,
                name = "밀리터리 프레스 (바벨)",
                category = shoulder,
                imageName = shoulder.imageName,
            ),
            sets = emptyList(),
        ),
    )

    FitzamTheme {
        HomeScreen(
            workouts = workouts,
            selectedDateWorkoutExercises = selectedDateWorkoutExercises,
            calendarState = rememberFitzamCalendarState(),
            onAddOrEditWorkout = {},
            onSettingsClick = {},
            onWorkoutExerciseClick = {},
        )
    }
}
