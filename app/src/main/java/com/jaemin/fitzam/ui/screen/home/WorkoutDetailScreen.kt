package com.jaemin.fitzam.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.ExerciseRecordSchema
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.screen.workoutrecord.EditableWorkoutSetUi
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordExerciseUiModel
import com.jaemin.fitzam.ui.screen.workoutrecord.WorkoutRecordViewModel
import com.jaemin.fitzam.ui.theme.ErrorRed
import com.jaemin.fitzam.ui.util.drawableResIdByName
import java.time.LocalDate

private val DECIMAL_INPUT_REGEX = Regex("^\\d*(\\.\\d{0,2})?$")
private val INT_INPUT_REGEX = Regex("^\\d*$")

@Composable
fun WorkoutDetailScreen(
    selectedDate: LocalDate,
    workoutExerciseId: Long,
    sessionId: Long,
    onDismissRequest: () -> Unit,
    onWorkoutStartClick: (Long, Long, String) -> Unit,
) {
    val viewModel: WorkoutRecordViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val exerciseItems by viewModel.exerciseItems.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.loadWorkoutForDate(selectedDate)
    }

    val selectedWorkoutExercise = exerciseItems.firstOrNull { item ->
        item.workoutExerciseId == workoutExerciseId
    }

    if (selectedWorkoutExercise == null) {
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        WorkoutDetailCard(
            exerciseItem = selectedWorkoutExercise,
            onDismissRequest = onDismissRequest,
            onFirstMetricChange = { setIndex, value ->
                viewModel.updateSetFirstMetric(
                    workoutExerciseId = selectedWorkoutExercise.workoutExerciseId,
                    setIndex = setIndex,
                    value = value,
                )
            },
            onSecondMetricChange = { setIndex, value ->
                viewModel.updateSetSecondMetric(
                    workoutExerciseId = selectedWorkoutExercise.workoutExerciseId,
                    setIndex = setIndex,
                    value = value,
                )
            },
            onSetDeleteClick = { setIndex ->
                viewModel.deleteSet(
                    workoutExerciseId = selectedWorkoutExercise.workoutExerciseId,
                    setIndex = setIndex,
                )
            },
            onStartClick = {
                onWorkoutStartClick(
                    selectedWorkoutExercise.workoutExerciseId,
                    selectedWorkoutExercise.exercise.id,
                    selectedWorkoutExercise.exercise.name,
                )
            },
            onCompleteClick = {
                viewModel.saveWorkout(
                    selectedDate = selectedDate,
                    onSuccess = onDismissRequest,
                )
            },
        )
    }
}

@Composable
private fun WorkoutDetailCard(
    exerciseItem: WorkoutRecordExerciseUiModel,
    onDismissRequest: () -> Unit,
    onFirstMetricChange: (Int, String) -> Unit,
    onSecondMetricChange: (Int, String) -> Unit,
    onSetDeleteClick: (Int) -> Unit,
    onStartClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(drawableResIdByName(exerciseItem.exercise.imageName)),
                            contentDescription = exerciseItem.exercise.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = exerciseItem.exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            ExerciseCategoryTag(
                                name = exerciseItem.exercise.category.name,
                                borderColor = Color(exerciseItem.exercise.category.colorHex),
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (exerciseItem.sets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "운동을 시작하면 세트가 추가됩니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    EditableWorkoutSetTable(
                        recordSchema = exerciseItem.exercise.recordSchema,
                        sets = exerciseItem.sets,
                        onFirstMetricChange = onFirstMetricChange,
                        onSecondMetricChange = onSecondMetricChange,
                        onRemoveSet = onSetDeleteClick,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedButton(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ErrorRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = ErrorRed,
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
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
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
    }
}

@Composable
private fun EditableWorkoutSetTable(
    recordSchema: ExerciseRecordSchema,
    sets: List<EditableWorkoutSetUi>,
    onFirstMetricChange: (Int, String) -> Unit,
    onSecondMetricChange: (Int, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeTableHeaderCell(text = "세트", modifier = Modifier.weight(1f))
            HomeTableHeaderCell(text = firstHeader, modifier = Modifier.weight(1f))
            HomeTableHeaderCell(text = secondHeader, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(24.dp))
        }

        sets.forEach { set ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeTableValueCell(text = set.index.toString(), modifier = Modifier.weight(1f))
                TableInputCell(
                    value = set.firstMetricText,
                    onValueChange = { nextValue ->
                        if (nextValue.matches(DECIMAL_INPUT_REGEX)) {
                            onFirstMetricChange(set.index, nextValue)
                        }
                    },
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                if (recordSchema == ExerciseRecordSchema.DISTANCE_DURATION) {
                    DurationInputCell(
                        totalSecondsText = set.secondMetricText,
                        onValueChange = { nextValue ->
                            if (nextValue.matches(INT_INPUT_REGEX)) {
                                onSecondMetricChange(set.index, nextValue)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    TableInputCell(
                        value = set.secondMetricText,
                        onValueChange = { nextValue ->
                            if (nextValue.matches(INT_INPUT_REGEX)) {
                                onSecondMetricChange(set.index, nextValue)
                            }
                        },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
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
