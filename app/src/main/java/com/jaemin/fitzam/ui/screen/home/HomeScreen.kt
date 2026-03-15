package com.jaemin.fitzam.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.jaemin.fitzam.ui.dzam.DZamAlertDialog
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.FitzamCalendar
import com.jaemin.fitzam.ui.dzam.FitzamCalendarDayList
import com.jaemin.fitzam.ui.dzam.FitzamCalendarState
import com.jaemin.fitzam.ui.dzam.FitzamFloatingActionButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.dzam.rememberFitzamCalendarState
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.theme.SuccessGreen
import com.jaemin.fitzam.ui.util.drawableResIdByName
import com.jaemin.fitzam.ui.util.formatDurationInMinutesAndSeconds
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val NO_DELETE_TARGET = -1L

@Composable
fun HomeScreen(
    onAddOrEditWorkout: (LocalDate) -> Unit,
    onAddWorkout: (LocalDate, Set<Long>) -> Unit,
    onWorkoutDetailClick: (LocalDate, Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val selectedDateWorkoutExercises by viewModel.selectedDateWorkoutExercises.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
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
        isEditMode = isEditMode,
        calendarState = calendarState,
        onAddOrEditWorkout = onAddOrEditWorkout,
        onAddWorkout = onAddWorkout,
        onSettingsClick = {
            viewModel.discardExerciseEdit()
            onSettingsClick()
        },
        onWorkoutExerciseClick = { exerciseId ->
            onWorkoutDetailClick(calendarState.selectedDate, exerciseId)
        },
        onWorkoutExerciseLongClick = viewModel::enterExerciseEdit,
        onMoveExerciseUp = viewModel::moveExerciseUp,
        onMoveExerciseDown = viewModel::moveExerciseDown,
        onDeleteExercise = viewModel::deleteExercise,
        onEditComplete = {
            viewModel.saveExerciseEdit(onSuccess = {})
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workouts: List<Workout>,
    selectedDateWorkoutExercises: List<WorkoutExercise>,
    isEditMode: Boolean,
    calendarState: FitzamCalendarState,
    onAddOrEditWorkout: (LocalDate) -> Unit,
    onAddWorkout: (LocalDate, Set<Long>) -> Unit,
    onSettingsClick: () -> Unit,
    onWorkoutExerciseClick: (Long) -> Unit,
    onWorkoutExerciseLongClick: () -> Unit,
    onMoveExerciseUp: (Long) -> Unit,
    onMoveExerciseDown: (Long) -> Unit,
    onDeleteExercise: (Long) -> Unit,
    onEditComplete: () -> Unit,
) {
    var deleteConfirmExerciseId by rememberSaveable { mutableLongStateOf(NO_DELETE_TARGET) }
    val deleteConfirmTarget = selectedDateWorkoutExercises.firstOrNull { workoutExercise ->
        workoutExercise.exercise.id == deleteConfirmExerciseId
    }
    val selectedDateWorkout = workouts.firstOrNull { it.date == calendarState.selectedDate }
    val hasRecordedCategories = selectedDateWorkout?.exerciseCategories?.isNotEmpty() == true
    val hasWorkoutExercises = selectedDateWorkoutExercises.isNotEmpty()
    val selectedCategoryIds = selectedDateWorkout?.exerciseCategories
        ?.map { category -> category.id }
        ?.toSet()
        .orEmpty()

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
            if (!isEditMode) {
                FitzamFloatingActionButton(
                    icon = if (workouts.any { it.date == calendarState.selectedDate }) {
                        ImageVector.vectorResource(R.drawable.ic_edit)
                    } else {
                        ImageVector.vectorResource(R.drawable.ic_plus)
                    },
                    onClick = { onAddOrEditWorkout(calendarState.selectedDate) },
                )
            }
        },
        bottomBar = {
            if (isEditMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 12.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
                        ),
                ) {
                    DZamButton(
                        text = "완료",
                        onClick = onEditComplete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = Color.White,
                            disabledContainerColor = SuccessGreen,
                            disabledContentColor = Color.White,
                        ),
                    )
                }
            }
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
                selectedDateWorkout?.exerciseCategories?.forEach { category ->
                    ExerciseCategoryTag(
                        name = category.name,
                        borderColor = Color(category.colorHex),
                    )
                }
            }

            if (hasWorkoutExercises) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    selectedDateWorkoutExercises.forEachIndexed { index, workoutExercise ->
                        HomeWorkoutExerciseCard(
                            workoutExercise = workoutExercise,
                            isEditMode = isEditMode,
                            canMoveUp = index > 0,
                            canMoveDown = index < selectedDateWorkoutExercises.lastIndex,
                            onClick = { onWorkoutExerciseClick(workoutExercise.exercise.id) },
                            onLongClick = onWorkoutExerciseLongClick,
                            onMoveUp = { onMoveExerciseUp(workoutExercise.exercise.id) },
                            onMoveDown = { onMoveExerciseDown(workoutExercise.exercise.id) },
                            onDelete = {
                                if (workoutExercise.sets.isEmpty()) {
                                    onDeleteExercise(workoutExercise.exercise.id)
                                } else {
                                    deleteConfirmExerciseId = workoutExercise.exercise.id
                                }
                            },
                        )
                    }
                    if (!isEditMode) {
                        HomeAddWorkoutButton(
                            onClick = {
                                onAddWorkout(
                                    calendarState.selectedDate,
                                    selectedCategoryIds,
                                )
                            },
                        )
                    }
                }
            } else if (hasRecordedCategories && !isEditMode) {
                HomeAddWorkoutButton(
                    onClick = {
                        onAddWorkout(
                            calendarState.selectedDate,
                            selectedCategoryIds,
                        )
                    },
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                HomeEmptyRecordState(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(if (isEditMode) 104.dp else 88.dp))
        }
    }

    if (deleteConfirmTarget != null) {
        DZamAlertDialog(
            title = "운동 삭제",
            text = "세트 기록이 있는 운동입니다.\n운동을 정말 삭제할까요?",
            onConfirm = {
                onDeleteExercise(deleteConfirmTarget.exercise.id)
                deleteConfirmExerciseId = NO_DELETE_TARGET
            },
            onCancel = {
                deleteConfirmExerciseId = NO_DELETE_TARGET
            },
            confirmText = "삭제",
            cancelText = "취소",
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeWorkoutExerciseCard(
    workoutExercise: WorkoutExercise,
    isEditMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.then(
            if (isEditMode) {
                Modifier
            } else {
                Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isEditMode) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EditMoveButton(
                        iconResId = R.drawable.ic_up,
                        contentDescription = "위로 이동",
                        enabled = canMoveUp,
                        onClick = onMoveUp,
                    )
                    EditMoveButton(
                        iconResId = R.drawable.ic_down,
                        contentDescription = "아래로 이동",
                        enabled = canMoveDown,
                        onClick = onMoveDown,
                    )
                }
            }

            Image(
                painter = painterResource(drawableResIdByName(workoutExercise.exercise.imageName)),
                contentDescription = workoutExercise.exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = workoutExercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExerciseCategoryTag(
                    name = workoutExercise.exercise.category.name,
                    borderColor = Color(workoutExercise.exercise.category.colorHex),
                )

                if (!isEditMode && workoutExercise.sets.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeWorkoutSetTable(
                        recordSchema = workoutExercise.exercise.recordSchema,
                        sets = workoutExercise.sets,
                    )
                }
            }

            if (isEditMode) {
                IconButton(
                    onClick = onDelete,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_trash),
                        contentDescription = "운동 삭제",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAddWorkoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DZamButton(
        text = "운동 추가",
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun HomeEmptyRecordState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_memo),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(52.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "운동을 기록하세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EditMoveButton(
    iconResId: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(44.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = contentDescription,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )
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
            isEditMode = true,
            calendarState = rememberFitzamCalendarState(),
            onAddOrEditWorkout = {},
            onAddWorkout = { _, _ -> },
            onSettingsClick = {},
            onWorkoutExerciseClick = {},
            onWorkoutExerciseLongClick = {},
            onMoveExerciseUp = {},
            onMoveExerciseDown = {},
            onDeleteExercise = {},
            onEditComplete = {},
        )
    }
}
