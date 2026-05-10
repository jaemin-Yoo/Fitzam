package com.jaemin.fitzam.ui.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamAlertDialog
import com.jaemin.fitzam.ui.screen.workoutrecord.EditableWorkoutSetUi
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutUiModel
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordUiState
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordViewModel
import com.jaemin.fitzam.ui.theme.ErrorRed
import com.jaemin.fitzam.ui.util.metricHeader
import com.jaemin.fitzam.ui.util.drawableResIdByName
import java.time.LocalDate

private val DECIMAL_INPUT_REGEX = Regex("^\\d*(\\.\\d{0,2})?$")
private val INT_INPUT_REGEX = Regex("^\\d*$")

@Composable
fun WorkoutDetailScreen(
    selectedDate: LocalDate,
    workoutId: Long,
    exerciseId: Long,
    sessionId: Long,
    onDismissRequest: () -> Unit,
    onWorkoutStartClick: (Long, Long, String) -> Unit,
) {
    val viewModel: WorkoutRecordViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val workoutItems by viewModel.workoutItems.collectAsStateWithLifecycle()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate) {
        viewModel.loadWorkoutForDate(selectedDate)
    }

    val selectedWorkout = workoutItems.firstOrNull { item ->
        item.workoutId == workoutId
    } ?: workoutItems.firstOrNull { item ->
        item.exercise.id == exerciseId
    }

    if (uiState is WorkoutRecordUiState.Loading) {
        WorkoutDetailLoadingScreen(onDismissRequest = onDismissRequest)
        return
    }

    if (selectedWorkout == null) {
        LaunchedEffect(selectedDate, workoutId) {
            onDismissRequest()
        }
        return
    }

    val handleDismissRequest = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onDismissRequest()
        }
    }

    BackHandler {
        handleDismissRequest()
    }

    Scaffold(
        topBar = {
            WorkoutDetailTopAppBar(
                title = selectedWorkout.exercise.name,
                onBackClick = handleDismissRequest,
            )
        },
        containerColor = Color.White,
        bottomBar = {
            WorkoutDetailBottomBar(
                onCompleteClick = {
                    viewModel.saveWorkout(
                        selectedDate = selectedDate,
                        onSuccess = onDismissRequest,
                    )
                },
            )
        },
    ) { paddingValues ->
        WorkoutDetailContent(
            workoutItem = selectedWorkout,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp,
                ),
            onMetricChange = { setIndex, metricType, value ->
                viewModel.updateSetMetric(
                    workoutId = selectedWorkout.workoutId,
                    setIndex = setIndex,
                    metricType = metricType,
                    value = value,
                )
            },
            onStartClick = {
                onWorkoutStartClick(
                    selectedWorkout.workoutId,
                    selectedWorkout.exercise.id,
                    selectedWorkout.exercise.name,
                )
            },
            onSetDeleteClick = { setIndex ->
                viewModel.deleteSet(
                    workoutId = selectedWorkout.workoutId,
                    setIndex = setIndex,
                )
            },
        )
    }

    if (showDiscardDialog) {
        DZamAlertDialog(
            title = "저장되지 않은 변경사항",
            text = "변경된 내용이 있는데 나가면 저장이 안 됩니다.\n정말 나갈까요?",
            onConfirm = {
                viewModel.discardDraftChanges()
                showDiscardDialog = false
                onDismissRequest()
            },
            onCancel = {
                showDiscardDialog = false
            },
            confirmText = "나가기",
            cancelText = "취소",
        )
    }
}

@Composable
private fun WorkoutDetailLoadingScreen(
    onDismissRequest: () -> Unit,
) {
    Scaffold(
        topBar = {
            WorkoutDetailTopAppBar(
                title = "운동 상세",
                onBackClick = onDismissRequest,
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailTopAppBar(
    title: String,
    onBackClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
    )
}

@Composable
private fun WorkoutDetailContent(
    workoutItem: WorkoutUiModel,
    modifier: Modifier = Modifier,
    onMetricChange: (Int, WorkoutMetricType, String) -> Unit,
    onStartClick: () -> Unit,
    onSetDeleteClick: (Int) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(drawableResIdByName(workoutItem.exercise.imageName)),
                        contentDescription = workoutItem.exercise.name,
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
                            text = workoutItem.exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        ExerciseCategoryTag(
                            name = workoutItem.exercise.category.name,
                            borderColor = Color(workoutItem.exercise.category.colorHex),
                        )
                    }
                }

                if (workoutItem.sets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "운동 시작 버튼을 눌러 첫 세트를 기록해 주세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    EditableWorkoutSetTable(
                        metricTypes = workoutItem.exercise.metricTypes,
                        sets = workoutItem.sets,
                        onMetricChange = onMetricChange,
                        onRemoveSet = onSetDeleteClick,
                    )
                }
            }
        }

        WorkoutStartButton(onClick = onStartClick)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WorkoutDetailBottomBar(
    onCompleteClick: () -> Unit,
) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onCompleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = "완료",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun WorkoutStartButton(
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "운동 시작",
                style = MaterialTheme.typography.labelLarge,
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_play),
                contentDescription = "운동 시작",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun EditableWorkoutSetTable(
    metricTypes: List<WorkoutMetricType>,
    sets: List<EditableWorkoutSetUi>,
    onMetricChange: (Int, WorkoutMetricType, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeTableHeaderCell(text = "세트", modifier = Modifier.weight(1f))
            metricTypes.forEach { metricType ->
                HomeTableHeaderCell(
                    text = metricHeader(metricType),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.width(24.dp))
        }

        sets.forEach { set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HomeTableValueCell(text = set.index.toString(), modifier = Modifier.weight(1f))
                metricTypes.forEach { metricType ->
                    if (metricType == WorkoutMetricType.DURATION_SEC) {
                        DurationInputCell(
                            totalSecondsText = set.metricValues[metricType].orEmpty(),
                            onValueChange = { nextValue ->
                                if (nextValue.matches(INT_INPUT_REGEX)) {
                                    onMetricChange(set.index, metricType, nextValue)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        TableInputCell(
                            value = set.metricValues[metricType].orEmpty(),
                            onValueChange = { nextValue ->
                                val isValid = when (metricType) {
                                    WorkoutMetricType.WEIGHT_KG,
                                    WorkoutMetricType.DISTANCE_KM -> nextValue.matches(DECIMAL_INPUT_REGEX)
                                    WorkoutMetricType.REPS -> nextValue.matches(INT_INPUT_REGEX)
                                    WorkoutMetricType.DURATION_SEC -> true
                                }
                                if (isValid) {
                                    onMetricChange(set.index, metricType, nextValue)
                                }
                            },
                            keyboardType = when (metricType) {
                                WorkoutMetricType.WEIGHT_KG,
                                WorkoutMetricType.DISTANCE_KM -> KeyboardType.Decimal
                                WorkoutMetricType.REPS,
                                WorkoutMetricType.DURATION_SEC -> KeyboardType.Number
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                IconButton(
                    onClick = { onRemoveSet(set.index) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_minus_circle),
                        contentDescription = "세트 삭제",
                        tint = ErrorRed,
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationInputCell(
    totalSecondsText: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = totalSecondsText.toIntOrNull() ?: 0
    val minutesText = (totalSeconds / 60).toString()
    val secondsText = (totalSeconds % 60).toString()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableInputCell(
            value = minutesText,
            onValueChange = { nextMinutes ->
                if (nextMinutes.matches(INT_INPUT_REGEX)) {
                    val minutes = nextMinutes.toIntOrNull() ?: 0
                    val seconds = totalSeconds % 60
                    onValueChange((minutes * 60 + seconds).toString())
                }
            },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "분",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TableInputCell(
            value = secondsText,
            onValueChange = { nextSeconds ->
                if (nextSeconds.matches(INT_INPUT_REGEX)) {
                    val seconds = nextSeconds.toIntOrNull() ?: 0
                    if (seconds <= 59) {
                        val minutes = totalSeconds / 60
                        onValueChange((minutes * 60 + seconds).toString())
                    }
                }
            },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "초",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TableInputCell(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.widthIn(min = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        innerTextField()
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(20.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
    )
}
