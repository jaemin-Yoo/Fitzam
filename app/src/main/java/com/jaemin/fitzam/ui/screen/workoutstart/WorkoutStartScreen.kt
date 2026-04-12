package com.jaemin.fitzam.ui.screen.workoutstart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordViewModel
import com.jaemin.fitzam.ui.theme.ErrorRed
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.theme.SuccessGreen
import com.jaemin.fitzam.ui.util.formatDistanceDisplayValue
import com.jaemin.fitzam.ui.util.formatDurationShortLabel
import com.jaemin.fitzam.ui.util.formatMetricValue
import com.jaemin.fitzam.ui.util.metricLabel
import com.jaemin.fitzam.ui.util.metricQuickAdjustValues
import com.jaemin.fitzam.ui.util.metricStep
import java.time.LocalDate

private val DecimalInputRegex = Regex("^\\d*(\\.\\d{0,2})?$")
private val IntInputRegex = Regex("^\\d*$")

@Composable
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

    val sourceItem = exerciseItems.firstOrNull { item -> item.workoutExerciseId == workoutExerciseId }
    val initialValue = remember(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.exercise?.metricTypes,
    ) {
        viewModel.getEditorInitialValue(workoutExerciseId)
    }
    val selectedMetricNames = rememberSaveable(
        workoutExerciseId,
        sourceItem?.sets?.size,
        sourceItem?.exercise?.metricTypes,
    ) {
        mutableStateListOf(*initialValue.metricTypes.map { metricType -> metricType.name }.toTypedArray())
    }
    val metricInputs = remember(workoutExerciseId) {
        mutableStateMapOf<WorkoutMetricType, String>().apply {
            WorkoutMetricType.entries.forEach { metricType ->
                put(metricType, initialValue.metricValues[metricType].orZero())
            }
        }
    }

    val selectedMetricTypes = selectedMetricNames.map { name -> WorkoutMetricType.valueOf(name) }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = exerciseName,
                navigation = TopAppBarItem(
                    icon = IconSource.Vector(ImageVector.vectorResource(id = R.drawable.ic_back)),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                ),
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
                        viewModel.appendSet(
                            workoutExerciseId = workoutExerciseId,
                            metricTypes = selectedMetricTypes,
                            metricValues = selectedMetricTypes.associateWith { metricType ->
                                metricInputs[metricType].orZero()
                            },
                        )
                        onCompleteClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
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
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            MetricTypeSelector(
                selectedMetricTypes = selectedMetricTypes,
                onToggle = { metricType ->
                    when {
                        metricType in selectedMetricTypes && selectedMetricTypes.size > 1 -> {
                            selectedMetricNames.remove(metricType.name)
                        }

                        metricType !in selectedMetricTypes && selectedMetricTypes.size < 2 -> {
                            selectedMetricNames.add(metricType.name)
                        }
                    }
                },
            )

            selectedMetricTypes.forEach { metricType ->
                MetricSection(
                    metricType = metricType,
                    value = metricInputs[metricType].orZero(),
                    onValueChange = { nextValue ->
                        if (isValidMetricInput(metricType, nextValue)) {
                            metricInputs[metricType] = nextValue
                        }
                    },
                    onStepChange = { delta ->
                        metricInputs[metricType] = adjustedMetricValue(
                            metricType = metricType,
                            currentValue = metricInputs[metricType].orZero(),
                            delta = delta,
                        )
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricTypeSelector(
    selectedMetricTypes: List<WorkoutMetricType>,
    onToggle: (WorkoutMetricType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "기록 유형",
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkoutMetricType.entries.forEach { metricType ->
                FilterChip(
                    selected = metricType in selectedMetricTypes,
                    onClick = { onToggle(metricType) },
                    label = { Text(metricLabel(metricType)) },
                )
            }
        }
        Text(
            text = "최대 2개까지 선택할 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricSection(
    metricType: WorkoutMetricType,
    value: String,
    onValueChange: (String) -> Unit,
    onStepChange: (Double) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = metricLabel(metricType),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleAdjustButton(
                isDecrease = true,
                onClick = { onStepChange(-metricStep(metricType)) },
            )
            MetricValueDisplay(
                metricType = metricType,
                value = value,
                onValueChange = onValueChange,
            )
            CircleAdjustButton(
                isDecrease = false,
                onClick = { onStepChange(metricStep(metricType)) },
            )
        }

        MetricQuickAdjustButtons(
            metricType = metricType,
            onAdjust = onStepChange,
        )
    }
}

@Composable
private fun MetricValueDisplay(
    metricType: WorkoutMetricType,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(color = Color.Transparent) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardTypeForMetric(metricType),
                    imeAction = ImeAction.Done,
                ),
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.widthIn(min = 84.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        innerTextField()
                    }
                },
            )
            val secondaryLabel = displaySecondaryValue(metricType, value)
            if (secondaryLabel != null) {
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun MetricQuickAdjustButtons(
    metricType: WorkoutMetricType,
    onAdjust: (Double) -> Unit,
) {
    val values = metricQuickAdjustValues(metricType)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            values.forEach { quickValue ->
                QuickAdjustButton(
                    text = "+",
                    caption = quickAdjustCaption(metricType, quickValue),
                    backgroundColor = SuccessGreen,
                    onClick = { onAdjust(quickValue) },
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            values.forEach { quickValue ->
                QuickAdjustButton(
                    text = "-",
                    caption = quickAdjustCaption(metricType, quickValue),
                    backgroundColor = ErrorRed,
                    onClick = { onAdjust(-quickValue) },
                )
            }
        }
    }
}

@Composable
private fun QuickAdjustButton(
    text: String,
    caption: String,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.dp, Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(52.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun CircleAdjustButton(
    isDecrease: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
    ) {
        if (isDecrease) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_minus_circle),
                contentDescription = "감소",
                tint = Color.Unspecified,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_plus),
                    contentDescription = "증가",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun displaySecondaryValue(
    metricType: WorkoutMetricType,
    value: String,
): String? {
    val numericValue = metricValueAsDouble(metricType, value)
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> "KG"
        WorkoutMetricType.REPS -> "개"
        WorkoutMetricType.DISTANCE_KM -> {
            val totalMeters = (numericValue * 1000).toInt()
            val km = totalMeters / 1000
            val meters = totalMeters % 1000
            if (km > 0 && meters > 0) "KM ${meters} M" else if (km > 0) "KM" else "M"
        }
        WorkoutMetricType.DURATION_SEC -> {
            val totalSeconds = numericValue.toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            if (minutes > 0 && seconds > 0) "분 ${seconds}초" else if (minutes > 0) "분" else "초"
        }
    }
}

private fun quickAdjustCaption(
    metricType: WorkoutMetricType,
    value: Double,
): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.REPS -> formatMetricValue(value)
        WorkoutMetricType.DISTANCE_KM -> formatDistanceDisplayValue(value)
        WorkoutMetricType.DURATION_SEC -> formatDurationShortLabel(value.toInt())
    }
}

private fun adjustedMetricValue(
    metricType: WorkoutMetricType,
    currentValue: String,
    delta: Double,
): String {
    val nextValue = (metricValueAsDouble(metricType, currentValue) + delta).coerceAtLeast(0.0)
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> formatMetricValue(nextValue)
        WorkoutMetricType.REPS,
        WorkoutMetricType.DURATION_SEC -> nextValue.toInt().toString()
    }
}

private fun metricValueAsDouble(
    metricType: WorkoutMetricType,
    value: String,
): Double {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> value.toDoubleOrNull() ?: 0.0
        WorkoutMetricType.REPS,
        WorkoutMetricType.DURATION_SEC -> (value.toIntOrNull() ?: 0).toDouble()
    }
}

private fun isValidMetricInput(
    metricType: WorkoutMetricType,
    value: String,
): Boolean {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> value.matches(DecimalInputRegex)
        WorkoutMetricType.REPS,
        WorkoutMetricType.DURATION_SEC -> value.matches(IntInputRegex)
    }
}

private fun keyboardTypeForMetric(metricType: WorkoutMetricType): KeyboardType {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.DISTANCE_KM -> KeyboardType.Decimal
        WorkoutMetricType.REPS,
        WorkoutMetricType.DURATION_SEC -> KeyboardType.Number
    }
}

private fun String?.orZero(): String = if (this.isNullOrBlank()) "0" else this

@Preview(showBackground = true)
@Composable
private fun WorkoutStartPreview() {
    FitzamTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            MetricTypeSelector(
                selectedMetricTypes = listOf(
                    WorkoutMetricType.WEIGHT_KG,
                    WorkoutMetricType.REPS,
                ),
                onToggle = {},
            )
            MetricSection(
                metricType = WorkoutMetricType.WEIGHT_KG,
                value = "80",
                onValueChange = {},
                onStepChange = {},
            )
            MetricSection(
                metricType = WorkoutMetricType.REPS,
                value = "10",
                onValueChange = {},
                onStepChange = {},
            )
        }
    }
}
