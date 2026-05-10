package com.jaemin.fitzam.ui.screen.workoutstart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import com.jaemin.fitzam.ui.util.formatDurationShortLabel
import com.jaemin.fitzam.ui.util.formatMetricValue
import com.jaemin.fitzam.ui.util.metricLabel
import com.jaemin.fitzam.ui.util.metricStep
import java.time.LocalDate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DecimalInputRegex = Regex("^\\d*(\\.\\d{0,2})?$")
private val IntInputRegex = Regex("^\\d*$")
private const val MetricSelectionLimitErrorMessage = "최대 2개까지만 선택할 수 있습니다."
private const val AdjustRepeatInitialDelayMillis = 350L
private const val AdjustRepeatDelayMillis = 70L
private val WorkoutStartSurfaceVariant = Color(0xFFDFDFDF)
private val WorkoutStartOnSurfaceVariant = Color(0xFF808080)

@Composable
fun WorkoutStartScreen(
    selectedDate: LocalDate,
    workoutId: Long,
    exerciseId: Long,
    exerciseName: String,
    sessionId: Long,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val viewModel: WorkoutRecordViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val workoutItems by viewModel.workoutItems.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.loadWorkoutForDate(selectedDate)
    }

    val sourceItem = workoutItems.firstOrNull { item -> item.workoutId == workoutId }
    val initialValue = remember(
        workoutId,
        sourceItem?.sets?.size,
        sourceItem?.exercise?.metricTypes,
    ) {
        viewModel.getEditorInitialValue(workoutId)
    }
    val selectedMetricNames = rememberSaveable(
        workoutId,
        sourceItem?.sets?.size,
        sourceItem?.exercise?.metricTypes,
    ) {
        mutableStateListOf(*initialValue.metricTypes.map { metricType -> metricType.name }.toTypedArray())
    }
    val metricInputs = remember(workoutId) {
        mutableStateMapOf<WorkoutMetricType, String>().apply {
            WorkoutMetricType.entries.forEach { metricType ->
                put(metricType, initialValue.metricValues[metricType].orZero())
            }
        }
    }

    val selectedMetricTypes = selectedMetricNames.map { name -> WorkoutMetricType.valueOf(name) }
    val isMetricSelectionLimitExceeded = selectedMetricTypes.size > 2
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
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
                        if (isMetricSelectionLimitExceeded) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(MetricSelectionLimitErrorMessage)
                            }
                            return@DZamButton
                        }
                        viewModel.appendSet(
                            workoutId = workoutId,
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            MetricTypeSelector(
                selectedMetricTypes = selectedMetricTypes,
                isError = isMetricSelectionLimitExceeded,
                onToggle = { metricType ->
                    if (metricType in selectedMetricTypes) {
                        selectedMetricNames.remove(metricType.name)
                    } else {
                        selectedMetricNames.add(metricType.name)
                    }
                },
            )

            selectedMetricTypes.forEach { metricType ->
                MetricControlSection(
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
    isError: Boolean,
    onToggle: (WorkoutMetricType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WorkoutMetricType.entries.forEach { metricType ->
                val selected = metricType in selectedMetricTypes
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.primary else WorkoutStartSurfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else WorkoutStartOnSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggle(metricType) },
                ) {
                    Text(
                        text = metricLabel(metricType),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = MetricSelectionLimitErrorMessage,
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed,
            )
        }
    }
}

@Composable
private fun MetricControlSection(
    metricType: WorkoutMetricType,
    value: String,
    onValueChange: (String) -> Unit,
    onStepChange: (Double) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = metricLabel(metricType),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        MetricValueCard(
            metricType = metricType,
            value = value,
            onValueChange = onValueChange,
            onDecrease = { onStepChange(-workoutStartStep(metricType)) },
            onIncrease = { onStepChange(workoutStartStep(metricType)) },
        )

        MetricQuickAdjustButtons(
            metricType = metricType,
            onAdjust = onStepChange,
        )
    }
}

@Composable
private fun MetricValueCard(
    metricType: WorkoutMetricType,
    value: String,
    onValueChange: (String) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(129.dp)
                .padding(horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SquareAdjustButton(
                text = "-",
                contentDescription = "감소",
                onClick = onDecrease,
            )
            MetricValueDisplay(
                metricType = metricType,
                value = value,
                onValueChange = onValueChange,
            )
            SquareAdjustButton(
                text = "+",
                contentDescription = "증가",
                onClick = onIncrease,
            )
        }
    }
}

@Composable
private fun MetricValueDisplay(
    metricType: WorkoutMetricType,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.width(if (metricType == WorkoutMetricType.DURATION_SEC) 156.dp else 128.dp),
    ) {
        if (metricType == WorkoutMetricType.DURATION_SEC) {
            DurationValueDisplay(value = value, onValueChange = onValueChange)
        } else {
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
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        innerTextField()
                    }
                },
            )
        }
        displaySecondaryValue(metricType, value)?.let { secondaryLabel ->
            Text(
                text = secondaryLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DurationValueDisplay(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val totalSeconds = value.toIntOrNull() ?: 0
    var hoursText by remember { mutableStateOf((totalSeconds / 3600).toString().padStart(2, '0')) }
    var minutesText by remember { mutableStateOf(((totalSeconds % 3600) / 60).toString().padStart(2, '0')) }
    var secondsText by remember { mutableStateOf((totalSeconds % 60).toString().padStart(2, '0')) }
    val suppressSync = remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (suppressSync.value) {
            suppressSync.value = false
            return@LaunchedEffect
        }
        val total = value.toIntOrNull() ?: 0
        hoursText = (total / 3600).toString().padStart(2, '0')
        minutesText = ((total % 3600) / 60).toString().padStart(2, '0')
        secondsText = (total % 60).toString().padStart(2, '0')
    }

    fun emit() {
        val h = hoursText.toIntOrNull() ?: 0
        val m = (minutesText.toIntOrNull() ?: 0).coerceIn(0, 59)
        val s = (secondsText.toIntOrNull() ?: 0).coerceIn(0, 59)
        suppressSync.value = true
        onValueChange(((h * 3600) + (m * 60) + s).toString())
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        DurationPartField(
            value = hoursText,
            label = "시",
            onValueChange = { new ->
                if (new.length <= 2 && new.matches(IntInputRegex)) {
                    hoursText = new
                    emit()
                }
            },
            modifier = Modifier.weight(1f),
        )
        DurationSeparatorText()
        DurationPartField(
            value = minutesText,
            label = "분",
            onValueChange = { new ->
                if (new.length <= 2 && new.matches(IntInputRegex)) {
                    minutesText = new
                    emit()
                }
            },
            modifier = Modifier.weight(1f),
        )
        DurationSeparatorText()
        DurationPartField(
            value = secondsText,
            label = "초",
            onValueChange = { new ->
                if (new.length <= 2 && new.matches(IntInputRegex)) {
                    secondsText = new
                    emit()
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DurationPartField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            textStyle = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DurationSeparatorText() {
    Text(
        text = ":",
        style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 36.sp,
            lineHeight = 44.sp,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 16.dp),
    )
}

@Composable
private fun MetricQuickAdjustButtons(
    metricType: WorkoutMetricType,
    onAdjust: (Double) -> Unit,
) {
    val values = workoutStartQuickAdjustValues(metricType)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { quickValue ->
                QuickAdjustButton(
                    text = "+ ${quickAdjustCaption(metricType, quickValue)}",
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    onClick = { onAdjust(quickValue) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { quickValue ->
                QuickAdjustButton(
                    text = "- ${quickAdjustCaption(metricType, quickValue)}",
                    backgroundColor = WorkoutStartSurfaceVariant,
                    onClick = { onAdjust(-quickValue) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuickAdjustButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SquareAdjustButton(
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(52.dp),
        color = WorkoutStartSurfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .repeatOnPress(onClick)
                .semantics {
                    this.contentDescription = contentDescription
                    role = Role.Button
                    onClick(label = contentDescription) {
                        onClick()
                        true
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Modifier.repeatOnPress(onClick: () -> Unit): Modifier = pointerInput(onClick) {
    detectTapGestures(
        onPress = {
            onClick()
            coroutineScope {
                val repeatJob = launch {
                    delay(AdjustRepeatInitialDelayMillis)
                    while (true) {
                        onClick()
                        delay(AdjustRepeatDelayMillis)
                    }
                }
                tryAwaitRelease()
                repeatJob.cancel()
            }
        },
    )
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
            "KM"
        }
        WorkoutMetricType.DURATION_SEC -> null
    }
}

private fun workoutStartStep(metricType: WorkoutMetricType): Double {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> 0.5
        WorkoutMetricType.DURATION_SEC -> 1.0
        else -> metricStep(metricType)
    }
}

private fun workoutStartQuickAdjustValues(metricType: WorkoutMetricType): List<Double> {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG -> listOf(2.5, 5.0, 10.0, 15.0, 20.0)
        WorkoutMetricType.REPS -> listOf(5.0, 10.0, 50.0)
        WorkoutMetricType.DISTANCE_KM -> listOf(0.5, 1.0, 3.0)
        WorkoutMetricType.DURATION_SEC -> listOf(10.0, 60.0, 300.0)
    }
}

private fun quickAdjustCaption(
    metricType: WorkoutMetricType,
    value: Double,
): String {
    return when (metricType) {
        WorkoutMetricType.WEIGHT_KG,
        WorkoutMetricType.REPS -> formatMetricValue(value)
        WorkoutMetricType.DISTANCE_KM -> formatMetricValue(value)
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

private fun formatDurationClock(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MetricTypeSelector(
                selectedMetricTypes = listOf(
                    WorkoutMetricType.WEIGHT_KG,
                    WorkoutMetricType.REPS,
                ),
                isError = false,
                onToggle = {},
            )
            MetricControlSection(
                metricType = WorkoutMetricType.WEIGHT_KG,
                value = "80",
                onValueChange = {},
                onStepChange = {},
            )
            MetricControlSection(
                metricType = WorkoutMetricType.REPS,
                value = "20",
                onValueChange = {},
                onStepChange = {},
            )
        }
    }
}
