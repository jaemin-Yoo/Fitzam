package com.jaemin.fitzam.ui.screen.workoutstart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.ExerciseRecordSchema
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordViewModel
import com.jaemin.fitzam.ui.screen.workoutrecord.formatWeightText
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate

private val WEIGHT_QUICK_VALUES = listOf(2.5, 5.0, 10.0, 15.0, 20.0)
private val REPS_QUICK_VALUES = listOf(5, 10, 50)
private val DISTANCE_QUICK_VALUES = listOf(0.5, 1.0, 2.0, 3.0, 5.0)
private val DURATION_QUICK_VALUES = listOf(10, 60, 300)
private val DECIMAL_INPUT_REGEX = Regex("^\\d*(\\.\\d{0,2})?$")
private val INT_INPUT_REGEX = Regex("^\\d*$")
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
@Suppress("UNUSED_PARAMETER")
fun WorkoutStartScreen(
    selectedDate: LocalDate,
    workoutExerciseId: Long,
    exerciseId: Long,
    exerciseName: String,
    sessionId: Long,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val viewModel: WorkoutRecordViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val exerciseItems by viewModel.exerciseItems.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.loadWorkoutForDate(selectedDate)
    }

    val sourceItem = exerciseItems.firstOrNull { item ->
        item.workoutExerciseId == workoutExerciseId
    }
    val initialValue = remember(
        workoutExerciseId,
        sourceItem?.exercise?.recordSchema,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.firstMetricText,
        sourceItem?.sets?.lastOrNull()?.secondMetricText,
    ) {
        viewModel.getEditorInitialValue(workoutExerciseId)
    }
    var recordSchema by rememberSaveable(
        workoutExerciseId,
        sourceItem?.exercise?.recordSchema?.name,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.firstMetricText,
        sourceItem?.sets?.lastOrNull()?.secondMetricText,
    ) { mutableStateOf(initialValue.recordSchema) }
    val config = remember(recordSchema) { metricConfig(recordSchema) }

    var firstValue by rememberSaveable(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.firstMetricText,
    ) { mutableStateOf(initialValue.firstValue) }
    var secondValue by rememberSaveable(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.secondMetricText,
    ) { mutableStateOf(initialValue.secondValue) }
    var firstInputText by rememberSaveable(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.firstMetricText,
    ) { mutableStateOf(formatWeightText(initialValue.firstValue)) }
    var secondInputText by rememberSaveable(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.sets?.lastOrNull()?.secondMetricText,
    ) { mutableStateOf(initialValue.secondValue.toString()) }
    var isFirstEditing by rememberSaveable(workoutExerciseId) { mutableStateOf(false) }
    var isSecondEditing by rememberSaveable(workoutExerciseId) { mutableStateOf(false) }

    val commitFirstEdit = {
        val parsed = firstInputText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        firstValue = parsed
        firstInputText = formatWeightText(parsed)
        isFirstEditing = false
    }
    val commitSecondEdit = {
        val parsed = secondInputText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        secondValue = parsed
        secondInputText = parsed.toString()
        isSecondEditing = false
    }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = exerciseName,
                navigation = TopAppBarItem(
                    icon = IconSource.Vector(ImageVector.vectorResource(id = R.drawable.ic_back)),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                ),
                actions = if (initialValue.isSchemaLocked) {
                    emptyList()
                } else {
                    listOf(
                        TopAppBarItem(
                            label = toggleLabel(recordSchema),
                            onClick = {
                                recordSchema = toggleRecordSchema(recordSchema)
                            },
                        ),
                    )
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
                    ),
            ) {
                DZamButton(
                    text = "완료",
                    onClick = {
                        commitFirstEdit()
                        commitSecondEdit()
                        viewModel.appendSet(
                            workoutExerciseId = workoutExerciseId,
                            firstValue = firstValue,
                            secondValue = secondValue,
                            recordSchema = recordSchema,
                        )
                        viewModel.saveWorkout(
                            selectedDate = selectedDate,
                            onSuccess = onCompleteClick,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { paddingValues ->
        WorkoutStartContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp,
                ),
            config = config,
            firstValue = firstValue,
            secondValue = secondValue,
            firstInputText = firstInputText,
            secondInputText = secondInputText,
            isFirstEditing = isFirstEditing,
            isSecondEditing = isSecondEditing,
            onFirstInputTextChange = { nextValue ->
                if (nextValue.matches(DECIMAL_INPUT_REGEX)) {
                    firstInputText = nextValue
                }
            },
            onSecondInputTextChange = { nextValue ->
                if (nextValue.matches(INT_INPUT_REGEX)) {
                    secondInputText = nextValue
                }
            },
            onFirstValueClick = {
                isFirstEditing = true
                firstInputText = formatWeightText(firstValue)
            },
            onSecondValueClick = {
                isSecondEditing = true
                secondInputText = secondValue.toString()
            },
            onFirstArrowDecrease = {
                firstValue = (firstValue - config.firstStep).coerceAtLeast(0.0)
                firstInputText = formatWeightText(firstValue)
            },
            onFirstArrowIncrease = {
                firstValue += config.firstStep
                firstInputText = formatWeightText(firstValue)
            },
            onSecondArrowDecrease = {
                secondValue = (secondValue - config.secondStep).coerceAtLeast(0)
                secondInputText = secondValue.toString()
            },
            onSecondArrowIncrease = {
                secondValue += config.secondStep
                secondInputText = secondValue.toString()
            },
            onFirstQuickIncrease = { delta ->
                firstValue += delta
                firstInputText = formatWeightText(firstValue)
            },
            onFirstQuickDecrease = { delta ->
                firstValue = (firstValue - delta).coerceAtLeast(0.0)
                firstInputText = formatWeightText(firstValue)
            },
            onSecondQuickIncrease = { delta ->
                secondValue += delta.toInt()
                secondInputText = secondValue.toString()
            },
            onSecondQuickDecrease = { delta ->
                secondValue = (secondValue - delta.toInt()).coerceAtLeast(0)
                secondInputText = secondValue.toString()
            },
            onFirstEditCommit = commitFirstEdit,
            onSecondEditCommit = commitSecondEdit,
        )
    }
}

@Composable
private fun WorkoutStartContent(
    modifier: Modifier,
    config: MetricConfig,
    firstValue: Double,
    secondValue: Int,
    firstInputText: String,
    secondInputText: String,
    isFirstEditing: Boolean,
    isSecondEditing: Boolean,
    onFirstInputTextChange: (String) -> Unit,
    onSecondInputTextChange: (String) -> Unit,
    onFirstValueClick: () -> Unit,
    onSecondValueClick: () -> Unit,
    onFirstArrowDecrease: () -> Unit,
    onFirstArrowIncrease: () -> Unit,
    onSecondArrowDecrease: () -> Unit,
    onSecondArrowIncrease: () -> Unit,
    onFirstQuickIncrease: (Double) -> Unit,
    onFirstQuickDecrease: (Double) -> Unit,
    onSecondQuickIncrease: (Double) -> Unit,
    onSecondQuickDecrease: (Double) -> Unit,
    onFirstEditCommit: () -> Unit,
    onSecondEditCommit: () -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ValueSection(
            title = config.firstLabel,
            value = config.firstValueFormatter(firstValue),
            valueInputText = firstInputText,
            isEditing = isFirstEditing,
            onInputChange = onFirstInputTextChange,
            onValueClick = onFirstValueClick,
            onEditCommit = onFirstEditCommit,
            onArrowDecrease = onFirstArrowDecrease,
            onArrowIncrease = onFirstArrowIncrease,
            keyboardType = KeyboardType.Decimal,
        )
        QuickAdjustGrid(
            values = config.firstQuickValues,
            formatter = config.firstQuickFormatter,
            onIncrease = onFirstQuickIncrease,
            onDecrease = onFirstQuickDecrease,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ValueSection(
            title = config.secondLabel,
            value = config.secondValueFormatter(secondValue),
            valueInputText = secondInputText,
            isEditing = isSecondEditing,
            onInputChange = onSecondInputTextChange,
            onValueClick = onSecondValueClick,
            onEditCommit = onSecondEditCommit,
            onArrowDecrease = onSecondArrowDecrease,
            onArrowIncrease = onSecondArrowIncrease,
            keyboardType = KeyboardType.Number,
        )
        QuickAdjustGrid(
            values = config.secondQuickValues,
            formatter = config.secondQuickFormatter,
            onIncrease = onSecondQuickIncrease,
            onDecrease = onSecondQuickDecrease,
            contentPadding = PaddingValues(bottom = 8.dp),
        )
    }
}

@Composable
private fun ValueSection(
    title: String,
    value: String,
    valueInputText: String,
    isEditing: Boolean,
    onInputChange: (String) -> Unit,
    onValueClick: () -> Unit,
    onEditCommit: () -> Unit,
    onArrowDecrease: () -> Unit,
    onArrowIncrease: () -> Unit,
    keyboardType: KeyboardType,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArrowAdjustButton(
                isLeft = true,
                onClick = onArrowDecrease,
            )

            if (isEditing) {
                BasicTextField(
                    value = valueInputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.width(140.dp),
                    textStyle = TextStyle(
                        fontSize = MaterialTheme.typography.displaySmall.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done,
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            innerTextField()
                        }
                    },
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onValueClick),
                )
            }

            ArrowAdjustButton(
                isLeft = false,
                onClick = onArrowIncrease,
            )
        }

        if (isEditing) {
            Text(
                text = "입력 완료",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable(onClick = onEditCommit),
            )
        }
    }
}

@Composable
private fun ArrowAdjustButton(
    isLeft: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_right_arrow),
            contentDescription = if (isLeft) "감소" else "증가",
            modifier = if (isLeft) Modifier.rotate(180f) else Modifier,
        )
    }
}

@Composable
private fun QuickAdjustGrid(
    values: List<Number>,
    formatter: (Double) -> String,
    onIncrease: (Double) -> Unit,
    onDecrease: (Double) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (values.size <= 3) 108.dp else 232.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        userScrollEnabled = false,
    ) {
        items(values) { value ->
            QuickAdjustButton(
                text = "+ ${formatter(value.toDouble())}",
                borderColor = SuccessGreen,
                textColor = SuccessGreen,
                onClick = { onIncrease(value.toDouble()) },
            )
        }
        items(values) { value ->
            QuickAdjustButton(
                text = "- ${formatter(value.toDouble())}",
                borderColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.primary,
                onClick = { onDecrease(value.toDouble()) },
            )
        }
    }
}

@Composable
private fun QuickAdjustButton(
    text: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private data class MetricConfig(
    val firstLabel: String,
    val secondLabel: String,
    val firstStep: Double,
    val secondStep: Int,
    val firstQuickValues: List<Number>,
    val secondQuickValues: List<Number>,
    val firstValueFormatter: (Double) -> String,
    val secondValueFormatter: (Int) -> String,
    val firstQuickFormatter: (Double) -> String,
    val secondQuickFormatter: (Double) -> String,
)

private fun metricConfig(recordSchema: ExerciseRecordSchema): MetricConfig {
    return when (recordSchema) {
        ExerciseRecordSchema.WEIGHT_REPS -> MetricConfig(
            firstLabel = "무게",
            secondLabel = "횟수",
            firstStep = 2.5,
            secondStep = 1,
            firstQuickValues = WEIGHT_QUICK_VALUES,
            secondQuickValues = REPS_QUICK_VALUES,
            firstValueFormatter = { value -> "${formatWeightText(value)}KG" },
            secondValueFormatter = { value -> "${value}회" },
            firstQuickFormatter = { value -> "${formatWeightText(value)}KG" },
            secondQuickFormatter = { value -> "${value.toInt()}회" },
        )
        ExerciseRecordSchema.DISTANCE_DURATION -> MetricConfig(
            firstLabel = "거리",
            secondLabel = "시간",
            firstStep = 0.1,
            secondStep = 1,
            firstQuickValues = DISTANCE_QUICK_VALUES,
            secondQuickValues = DURATION_QUICK_VALUES,
            firstValueFormatter = { value -> "${formatWeightText(value)}KM" },
            secondValueFormatter = { value -> formatDurationText(value) },
            firstQuickFormatter = { value -> "${formatWeightText(value)}KM" },
            secondQuickFormatter = { value -> formatDurationText(value.toInt()) },
        )
    }
}

private fun formatDurationText(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "0초"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val parts = buildList {
        if (hours > 0) add("${hours}시간")
        if (minutes > 0) add("${minutes}분")
        if (seconds > 0) add("${seconds}초")
    }
    return parts.joinToString(" ")
}

private fun toggleRecordSchema(current: ExerciseRecordSchema): ExerciseRecordSchema {
    return when (current) {
        ExerciseRecordSchema.WEIGHT_REPS -> ExerciseRecordSchema.DISTANCE_DURATION
        ExerciseRecordSchema.DISTANCE_DURATION -> ExerciseRecordSchema.WEIGHT_REPS
    }
}

private fun toggleLabel(current: ExerciseRecordSchema): String {
    return when (current) {
        ExerciseRecordSchema.WEIGHT_REPS -> "거리/시간"
        ExerciseRecordSchema.DISTANCE_DURATION -> "무게/횟수"
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutStartPreview() {
    FitzamTheme {
        WorkoutStartContent(
            modifier = Modifier.fillMaxSize(),
            config = metricConfig(ExerciseRecordSchema.WEIGHT_REPS),
            firstValue = 80.0,
            secondValue = 10,
            firstInputText = "80",
            secondInputText = "10",
            isFirstEditing = false,
            isSecondEditing = false,
            onFirstInputTextChange = {},
            onSecondInputTextChange = {},
            onFirstValueClick = {},
            onSecondValueClick = {},
            onFirstArrowDecrease = {},
            onFirstArrowIncrease = {},
            onSecondArrowDecrease = {},
            onSecondArrowIncrease = {},
            onFirstQuickIncrease = {},
            onFirstQuickDecrease = {},
            onSecondQuickIncrease = {},
            onSecondQuickDecrease = {},
            onFirstEditCommit = {},
            onSecondEditCommit = {},
        )
    }
}
