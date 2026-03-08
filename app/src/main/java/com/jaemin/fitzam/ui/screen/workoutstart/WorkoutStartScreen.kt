package com.jaemin.fitzam.ui.screen.workoutstart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jaemin.fitzam.R
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordViewModel
import com.jaemin.fitzam.ui.screen.workoutrecord.formatWeightText
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate

private val WEIGHT_QUICK_VALUES = listOf(2.5, 5.0, 10.0, 10.0, 15.0, 20.0)
private val REPS_QUICK_VALUES = listOf(5, 10, 50)
private val WEIGHT_INPUT_REGEX = Regex("^\\d*(\\.\\d{0,2})?$")
private val REPS_INPUT_REGEX = Regex("^\\d*$")
private val SuccessGreen = Color(0xFF4CAF50)

@Composable
@Suppress("UNUSED_PARAMETER")
fun WorkoutStartScreen(
    selectedDate: LocalDate,
    exerciseId: Long,
    exerciseName: String,
    sessionId: Long,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val viewModel: WorkoutRecordViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val initialValue = remember(exerciseId) {
        viewModel.getEditorInitialValue(exerciseId)
    }

    var weightKg by rememberSaveable(exerciseId) { mutableStateOf(initialValue.weightKg) }
    var reps by rememberSaveable(exerciseId) { mutableStateOf(initialValue.reps) }
    var weightInputText by rememberSaveable(exerciseId) { mutableStateOf(formatWeightText(initialValue.weightKg)) }
    var repsInputText by rememberSaveable(exerciseId) { mutableStateOf(initialValue.reps.toString()) }
    var isWeightEditing by rememberSaveable(exerciseId) { mutableStateOf(false) }
    var isRepsEditing by rememberSaveable(exerciseId) { mutableStateOf(false) }

    val commitWeightEdit = {
        val parsed = weightInputText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        weightKg = parsed
        weightInputText = formatWeightText(parsed)
        isWeightEditing = false
    }
    val commitRepsEdit = {
        val parsed = repsInputText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        reps = parsed
        repsInputText = parsed.toString()
        isRepsEditing = false
    }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = exerciseName,
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
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
                        commitWeightEdit()
                        commitRepsEdit()
                        viewModel.appendSet(
                            exerciseId = exerciseId,
                            weightKg = weightKg,
                            reps = reps,
                        )
                        onCompleteClick()
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
            weightKg = weightKg,
            reps = reps,
            weightInputText = weightInputText,
            repsInputText = repsInputText,
            isWeightEditing = isWeightEditing,
            isRepsEditing = isRepsEditing,
            onWeightInputTextChange = { nextValue ->
                if (nextValue.matches(WEIGHT_INPUT_REGEX)) {
                    weightInputText = nextValue
                }
            },
            onRepsInputTextChange = { nextValue ->
                if (nextValue.matches(REPS_INPUT_REGEX)) {
                    repsInputText = nextValue
                }
            },
            onWeightValueClick = {
                isWeightEditing = true
                weightInputText = formatWeightText(weightKg)
            },
            onRepsValueClick = {
                isRepsEditing = true
                repsInputText = reps.toString()
            },
            onWeightArrowDecrease = {
                weightKg = (weightKg - 2.5).coerceAtLeast(0.0)
                weightInputText = formatWeightText(weightKg)
            },
            onWeightArrowIncrease = {
                weightKg += 2.5
                weightInputText = formatWeightText(weightKg)
            },
            onRepsArrowDecrease = {
                reps = (reps - 1).coerceAtLeast(0)
                repsInputText = reps.toString()
            },
            onRepsArrowIncrease = {
                reps += 1
                repsInputText = reps.toString()
            },
            onWeightQuickIncrease = { delta ->
                weightKg += delta
                weightInputText = formatWeightText(weightKg)
            },
            onWeightQuickDecrease = { delta ->
                weightKg = (weightKg - delta).coerceAtLeast(0.0)
                weightInputText = formatWeightText(weightKg)
            },
            onRepsQuickIncrease = { delta ->
                reps += delta
                repsInputText = reps.toString()
            },
            onRepsQuickDecrease = { delta ->
                reps = (reps - delta).coerceAtLeast(0)
                repsInputText = reps.toString()
            },
            onWeightEditCommit = commitWeightEdit,
            onRepsEditCommit = commitRepsEdit,
        )
    }
}

@Composable
private fun WorkoutStartContent(
    modifier: Modifier,
    weightKg: Double,
    reps: Int,
    weightInputText: String,
    repsInputText: String,
    isWeightEditing: Boolean,
    isRepsEditing: Boolean,
    onWeightInputTextChange: (String) -> Unit,
    onRepsInputTextChange: (String) -> Unit,
    onWeightValueClick: () -> Unit,
    onRepsValueClick: () -> Unit,
    onWeightArrowDecrease: () -> Unit,
    onWeightArrowIncrease: () -> Unit,
    onRepsArrowDecrease: () -> Unit,
    onRepsArrowIncrease: () -> Unit,
    onWeightQuickIncrease: (Double) -> Unit,
    onWeightQuickDecrease: (Double) -> Unit,
    onRepsQuickIncrease: (Int) -> Unit,
    onRepsQuickDecrease: (Int) -> Unit,
    onWeightEditCommit: () -> Unit,
    onRepsEditCommit: () -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ValueSection(
            title = "무게",
            value = "${formatWeightText(weightKg)}KG",
            valueInputText = weightInputText,
            isEditing = isWeightEditing,
            onInputChange = onWeightInputTextChange,
            onValueClick = onWeightValueClick,
            onEditCommit = onWeightEditCommit,
            onArrowDecrease = onWeightArrowDecrease,
            onArrowIncrease = onWeightArrowIncrease,
            keyboardType = KeyboardType.Decimal,
        )
        QuickAdjustGrid(
            values = WEIGHT_QUICK_VALUES,
            formatter = { value -> formatWeightQuickLabel(value) },
            onIncrease = onWeightQuickIncrease,
            onDecrease = onWeightQuickDecrease,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ValueSection(
            title = "횟수",
            value = "${reps}회",
            valueInputText = repsInputText,
            isEditing = isRepsEditing,
            onInputChange = onRepsInputTextChange,
            onValueClick = onRepsValueClick,
            onEditCommit = onRepsEditCommit,
            onArrowDecrease = onRepsArrowDecrease,
            onArrowIncrease = onRepsArrowIncrease,
            keyboardType = KeyboardType.Number,
        )
        QuickAdjustGrid(
            values = REPS_QUICK_VALUES,
            formatter = { value -> "${value.toInt()}회" },
            onIncrease = { onRepsQuickIncrease(it.toInt()) },
            onDecrease = { onRepsQuickDecrease(it.toInt()) },
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

private fun formatWeightQuickLabel(value: Double): String {
    return "${formatWeightText(value)}KG"
}

@Preview(showBackground = true)
@Composable
private fun WorkoutStartPreview() {
    FitzamTheme {
        WorkoutStartContent(
            modifier = Modifier.fillMaxSize(),
            weightKg = 80.0,
            reps = 10,
            weightInputText = "80",
            repsInputText = "10",
            isWeightEditing = false,
            isRepsEditing = false,
            onWeightInputTextChange = {},
            onRepsInputTextChange = {},
            onWeightValueClick = {},
            onRepsValueClick = {},
            onWeightArrowDecrease = {},
            onWeightArrowIncrease = {},
            onRepsArrowDecrease = {},
            onRepsArrowIncrease = {},
            onWeightQuickIncrease = {},
            onWeightQuickDecrease = {},
            onRepsQuickIncrease = {},
            onRepsQuickDecrease = {},
            onWeightEditCommit = {},
            onRepsEditCommit = {},
        )
    }
}
