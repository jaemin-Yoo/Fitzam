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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.jaemin.fitzam.ui.screen.exercisecategoryselect.ExerciseCategorySelectBottomSheet
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
import com.jaemin.fitzam.model.Workout
import com.jaemin.fitzam.model.WorkoutRecord
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.model.WorkoutSet
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamAlertDialog
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.DZamCalendar
import com.jaemin.fitzam.ui.dzam.DZamCalendarEvent
import com.jaemin.fitzam.ui.dzam.DZamCalendarState
import com.jaemin.fitzam.ui.dzam.DZamOutlinedButton
import com.jaemin.fitzam.ui.dzam.rememberDZamCalendarState
import com.jaemin.fitzam.ui.dzam.FitzamFloatingActionButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.theme.SuccessGreen
import com.jaemin.fitzam.ui.util.drawableResIdByName
import com.jaemin.fitzam.ui.util.formatDurationDisplayValue
import com.jaemin.fitzam.ui.util.formatMetricValue
import com.jaemin.fitzam.ui.util.metricHeader
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val NO_DELETE_TARGET = -1L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddWorkout: (LocalDate, Set<Long>) -> Unit,
    onWorkoutDetailClick: (LocalDate, Long, Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val workoutRecords by viewModel.workoutRecords.collectAsStateWithLifecycle()
    val selectedDateWorkouts by viewModel.selectedDateWorkouts.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val calendarState = rememberDZamCalendarState()
    var showCategoryBottomSheet by rememberSaveable { mutableStateOf(false) }
    var categoryBottomSheetSessionId by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(calendarState.displayedYearMonth) {
        viewModel.loadWorkoutRecordsForYearMonth(calendarState.displayedYearMonth)
    }

    LaunchedEffect(calendarState.selectedDate) {
        viewModel.onSelectedDateChanged(calendarState.selectedDate)
    }

    HomeScreen(
        workoutRecords = workoutRecords,
        selectedDateWorkouts = selectedDateWorkouts,
        isEditMode = isEditMode,
        calendarState = calendarState,
        onAddOrEditWorkout = {
            categoryBottomSheetSessionId = System.currentTimeMillis()
            showCategoryBottomSheet = true
        },
        onAddWorkout = onAddWorkout,
        onSettingsClick = {
            viewModel.discardWorkoutEdit()
            onSettingsClick()
        },
        onWorkoutClick = { workoutId, exerciseId ->
            onWorkoutDetailClick(calendarState.selectedDate, workoutId, exerciseId)
        },
        onWorkoutLongClick = viewModel::enterWorkoutEdit,
        onMoveWorkoutUp = viewModel::moveWorkoutUp,
        onMoveWorkoutDown = viewModel::moveWorkoutDown,
        onDeleteWorkout = viewModel::deleteWorkout,
        onEditComplete = {
            viewModel.saveWorkoutEdit(onSuccess = {})
        },
    )

    if (showCategoryBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCategoryBottomSheet = false },
            sheetState = sheetState,
        ) {
            ExerciseCategorySelectBottomSheet(
                selectedDate = calendarState.selectedDate,
                sessionId = categoryBottomSheetSessionId,
                onCompleteClick = { showCategoryBottomSheet = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    workoutRecords: List<WorkoutRecord>,
    selectedDateWorkouts: List<Workout>,
    isEditMode: Boolean,
    calendarState: DZamCalendarState,
    onAddOrEditWorkout: (LocalDate) -> Unit,
    onAddWorkout: (LocalDate, Set<Long>) -> Unit,
    onSettingsClick: () -> Unit,
    onWorkoutClick: (Long, Long) -> Unit,
    onWorkoutLongClick: () -> Unit,
    onMoveWorkoutUp: (Long) -> Unit,
    onMoveWorkoutDown: (Long) -> Unit,
    onDeleteWorkout: (Long) -> Unit,
    onEditComplete: () -> Unit,
) {
    var deleteConfirmWorkoutId by rememberSaveable { mutableLongStateOf(NO_DELETE_TARGET) }
    val deleteConfirmTarget = selectedDateWorkouts.firstOrNull { workout ->
        workout.id == deleteConfirmWorkoutId
    }
    val selectedDateWorkoutRecord = workoutRecords.firstOrNull { it.date == calendarState.selectedDate }
    val hasRecordedCategories = selectedDateWorkoutRecord?.exerciseCategories?.isNotEmpty() == true
    val hasWorkouts = selectedDateWorkouts.isNotEmpty()
    val selectedCategoryIds = selectedDateWorkoutRecord?.exerciseCategories
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
                    icon = if (workoutRecords.any { it.date == calendarState.selectedDate }) {
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
            Box(modifier = Modifier.padding(vertical = 8.dp)) {
                DZamCalendar(
                    state = calendarState,
                    events = workoutRecords.associate { record ->
                        record.date to record.exerciseCategories.map { category ->
                            DZamCalendarEvent(
                                text = category.name,
                                backgroundColor = Color(category.colorHex),
                            )
                        }
                    },
                )
            }
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
                selectedDateWorkoutRecord?.exerciseCategories?.forEach { category ->
                    ExerciseCategoryTag(
                        name = category.name,
                        borderColor = Color(category.colorHex),
                    )
                }
            }

            if (hasWorkouts) {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    selectedDateWorkouts.forEachIndexed { index, workout ->
                        HomeWorkoutCard(
                            workout = workout,
                            isEditMode = isEditMode,
                            canMoveUp = index > 0,
                            canMoveDown = index < selectedDateWorkouts.lastIndex,
                            onClick = {
                                onWorkoutClick(
                                    workout.id,
                                    workout.exercise.id,
                                )
                            },
                            onLongClick = onWorkoutLongClick,
                            onMoveUp = { onMoveWorkoutUp(workout.id) },
                            onMoveDown = { onMoveWorkoutDown(workout.id) },
                            onDelete = {
                                if (workout.sets.isEmpty()) {
                                    onDeleteWorkout(workout.id)
                                } else {
                                    deleteConfirmWorkoutId = workout.id
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
                onDeleteWorkout(deleteConfirmTarget.id)
                deleteConfirmWorkoutId = NO_DELETE_TARGET
            },
            onCancel = {
                deleteConfirmWorkoutId = NO_DELETE_TARGET
            },
            confirmText = "삭제",
            cancelText = "취소",
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeWorkoutCard(
    workout: Workout,
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
        modifier = Modifier
            .fillMaxWidth()
            .then(
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
        if (isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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

                HomeWorkoutCardContent(
                    workout = workout,
                    showSets = false,
                    modifier = Modifier.weight(1f),
                )

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
        } else {
            HomeWorkoutCardContent(
                workout = workout,
                showSets = workout.sets.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun HomeWorkoutCardContent(
    workout: Workout,
    showSets: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(drawableResIdByName(workout.exercise.imageName)),
                contentDescription = workout.exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = workout.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ExerciseCategoryTag(
                    name = workout.exercise.category.name,
                    borderColor = Color(workout.exercise.category.colorHex),
                )
            }
        }

        if (showSets) {
            HomeWorkoutSetTable(
                metricTypes = workout.exercise.metricTypes,
                sets = workout.sets,
            )
        }
    }
}

@Composable
private fun HomeAddWorkoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DZamOutlinedButton(
        text = "운동 추가하기",
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = ImageVector.vectorResource(R.drawable.ic_plus),
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
    metricTypes: List<WorkoutMetricType>,
    sets: List<WorkoutSet>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeTableHeaderCell(text = "세트", modifier = Modifier.weight(1f))
            metricTypes.forEach { metricType ->
                HomeTableHeaderCell(
                    text = metricHeader(metricType),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        sets.forEach { set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeTableValueCell(text = set.index.toString(), modifier = Modifier.weight(1f))
                metricTypes.forEach { metricType ->
                    val metricValue = set.metrics[metricType] ?: 0.0
                    val metricText = when (metricType) {
                        WorkoutMetricType.DURATION_SEC -> formatDurationDisplayValue(metricValue.toInt())
                        else -> formatMetricValue(metricValue)
                    }
                    HomeTableValueCell(text = metricText, modifier = Modifier.weight(1f))
                }
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
        color = Color(0xFF808080),
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

    val workoutRecords = listOf(
        WorkoutRecord(
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
        WorkoutRecord(
            date = previewDate,
            exerciseCategories = listOf(chest, shoulder),
        ),
    )

    val selectedDateWorkouts = listOf(
        Workout(
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
        Workout(
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
            workoutRecords = workoutRecords,
            selectedDateWorkouts = selectedDateWorkouts,
            isEditMode = true,
            calendarState = rememberDZamCalendarState(),
            onAddOrEditWorkout = {},
            onAddWorkout = { _, _ -> },
            onSettingsClick = {},
            onWorkoutClick = { _, _ -> },
            onWorkoutLongClick = {},
            onMoveWorkoutUp = {},
            onMoveWorkoutDown = {},
            onDeleteWorkout = {},
            onEditComplete = {},
        )
    }
}
