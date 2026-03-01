package com.jaemin.fitzam.ui.screen.workoutadd

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.WorkoutSet
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.DZamOutlinedButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.theme.SuccessGreen
import com.jaemin.fitzam.ui.util.drawableResIdByName
import java.time.LocalDate
import java.util.Locale

private data class WorkoutAddExerciseItem(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
)

private data class EditableWorkoutSetUi(
    val index: Int,
    val weightText: String,
    val repsText: String,
)

private data class WorkoutAddExerciseUiState(
    val exercise: Exercise,
    val sets: List<EditableWorkoutSetUi>,
    val isEditing: Boolean = false,
)

@Composable
fun WorkoutAddScreen(
    selectedDate: LocalDate,
    selectedExerciseIds: Set<Long>,
    onBackClick: () -> Unit,
    onDetailAddClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val filteredItems = sampleWorkoutAddItems().filter { item ->
        selectedExerciseIds.isEmpty() || selectedExerciseIds.contains(item.exercise.id)
    }
    var exerciseItems by remember(filteredItems) {
        mutableStateOf(filteredItems.map { item -> item.toUiState() })
    }

    WorkoutAddScreen(
        selectedDate = selectedDate,
        exerciseItems = exerciseItems,
        onBackClick = onBackClick,
        onDetailAddClick = onDetailAddClick,
        onCompleteClick = onCompleteClick,
        onExerciseToggleEditClick = { exercise ->
            exerciseItems = exerciseItems.map { item ->
                if (item.exercise.id == exercise.id) {
                    item.copy(isEditing = !item.isEditing)
                } else {
                    item
                }
            }
        },
        onExerciseStartClick = {},
        onExerciseDeleteClick = { exercise ->
            exerciseItems = exerciseItems.filterNot { item -> item.exercise.id == exercise.id }
        },
        onSetWeightChange = { exercise, setIndex, weight ->
            exerciseItems = exerciseItems.map { item ->
                if (item.exercise.id == exercise.id) {
                    item.copy(
                        sets = item.sets.map { set ->
                            if (set.index == setIndex) {
                                set.copy(weightText = weight)
                            } else {
                                set
                            }
                        },
                    )
                } else {
                    item
                }
            }
        },
        onSetRepsChange = { exercise, setIndex, reps ->
            exerciseItems = exerciseItems.map { item ->
                if (item.exercise.id == exercise.id) {
                    item.copy(
                        sets = item.sets.map { set ->
                            if (set.index == setIndex) {
                                set.copy(repsText = reps)
                            } else {
                                set
                            }
                        },
                    )
                } else {
                    item
                }
            }
        },
        onSetDeleteClick = { exercise, setIndex ->
            exerciseItems = exerciseItems.map { item ->
                if (item.exercise.id == exercise.id) {
                    val reindexedSets = item.sets
                        .filterNot { set -> set.index == setIndex }
                        .mapIndexed { index, set ->
                            set.copy(index = index + 1)
                        }
                    item.copy(sets = reindexedSets)
                } else {
                    item
                }
            }
        },
    )
}

@Composable
private fun WorkoutAddScreen(
    selectedDate: LocalDate,
    exerciseItems: List<WorkoutAddExerciseUiState>,
    onBackClick: () -> Unit,
    onDetailAddClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onExerciseToggleEditClick: (Exercise) -> Unit,
    onExerciseStartClick: (Exercise) -> Unit,
    onExerciseDeleteClick: (Exercise) -> Unit,
    onSetWeightChange: (Exercise, Int, String) -> Unit,
    onSetRepsChange: (Exercise, Int, String) -> Unit,
    onSetDeleteClick: (Exercise, Int) -> Unit,
) {
    val selectedCategories = exerciseItems
        .map { item -> item.exercise.category }
        .distinctBy { category -> category.id }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 추가",
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
                    onClick = onCompleteClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "운동 부위",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedCategories.forEach { category ->
                        ExerciseCategoryTag(
                            name = category.name,
                            borderColor = Color(category.colorHex),
                        )
                    }
                }
            }

            items(
                items = exerciseItems,
                key = { it.exercise.id },
            ) { item ->
                WorkoutExerciseCard(
                    exerciseItem = item,
                    onEditClick = { onExerciseToggleEditClick(item.exercise) },
                    onStartClick = { onExerciseStartClick(item.exercise) },
                    onDeleteClick = { onExerciseDeleteClick(item.exercise) },
                    onSetWeightChange = { setIndex, weight ->
                        onSetWeightChange(item.exercise, setIndex, weight)
                    },
                    onSetRepsChange = { setIndex, reps ->
                        onSetRepsChange(item.exercise, setIndex, reps)
                    },
                    onSetDeleteClick = { setIndex ->
                        onSetDeleteClick(item.exercise, setIndex)
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WorkoutExerciseCard(
    exerciseItem: WorkoutAddExerciseUiState,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetWeightChange: (Int, String) -> Unit,
    onSetRepsChange: (Int, String) -> Unit,
    onSetDeleteClick: (Int) -> Unit,
) {
    val exercise = exerciseItem.exercise
    val sets = exerciseItem.sets

    Column {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(drawableResIdByName(exercise.imageName)),
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExerciseCategoryTag(
                            name = exercise.category.name,
                            borderColor = Color(exercise.category.colorHex),
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = if (exerciseItem.isEditing) {
                                ImageVector.vectorResource(id = R.drawable.ic_check)
                            } else {
                                ImageVector.vectorResource(id = R.drawable.ic_edit)
                            },
                            contentDescription = if (exerciseItem.isEditing) "편집 완료" else "운동 편집",
                            tint = if (exerciseItem.isEditing) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (sets.isEmpty()) {
                    Text(
                        text = "운동을 시작하세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 36.dp,
                                bottom = 60.dp,
                            ),
                    )
                } else {
                    WorkoutSetTable(
                        sets = sets,
                        isEditing = exerciseItem.isEditing,
                        onWeightChange = onSetWeightChange,
                        onRepsChange = onSetRepsChange,
                        onRemoveSet = onSetDeleteClick,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (exerciseItem.isEditing) {
            DZamButton(
                text = "삭제",
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_trash),
            )
        } else {
            DZamOutlinedButton(
                text = "운동 시작",
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                trailingIcon = ImageVector.vectorResource(id = R.drawable.ic_play),
            )
        }
    }
}

@Composable
private fun WorkoutSetTable(
    sets: List<EditableWorkoutSetUi>,
    isEditing: Boolean,
    onWeightChange: (Int, String) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            TableHeaderCell(text = "세트", modifier = Modifier.weight(1f))
            TableHeaderCell(text = "무게(KG)", modifier = Modifier.weight(1f))
            TableHeaderCell(text = "횟수", modifier = Modifier.weight(1f))
            if (isEditing) {
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.background)

        sets.forEach { set ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableValueCell(text = set.index.toString(), modifier = Modifier.weight(1f))
                if (isEditing) {
                    TableInputCell(
                        value = set.weightText,
                        onValueChange = { nextValue ->
                            if (nextValue.matches(WEIGHT_INPUT_REGEX)) {
                                onWeightChange(set.index, nextValue)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                    )
                    TableInputCell(
                        value = set.repsText,
                        onValueChange = { nextValue ->
                            if (nextValue.matches(REPS_INPUT_REGEX)) {
                                onRepsChange(set.index, nextValue)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number,
                    )
                    IconButton(
                        onClick = { onRemoveSet(set.index) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_minus_circle),
                            contentDescription = "세트 삭제",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    TableValueCell(text = set.weightText, modifier = Modifier.weight(1f))
                    TableValueCell(text = set.repsText, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TableValueCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
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

private val WEIGHT_INPUT_REGEX = Regex("^\\d*(\\.\\d{0,2})?$")
private val REPS_INPUT_REGEX = Regex("^\\d*$")

private fun WorkoutAddExerciseItem.toUiState(): WorkoutAddExerciseUiState {
    return WorkoutAddExerciseUiState(
        exercise = exercise,
        sets = sets.map { set ->
            EditableWorkoutSetUi(
                index = set.index,
                weightText = formatWeightText(set.weightKg),
                repsText = set.reps.toString(),
            )
        },
    )
}

private fun formatWeightText(weightKg: Double): String {
    return if (weightKg % 1.0 == 0.0) {
        weightKg.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", weightKg).trimEnd('0').trimEnd('.')
    }
}

private fun sampleWorkoutAddItems(): List<WorkoutAddExerciseItem> {
    val chest = ExerciseCategory(
        id = 0,
        name = "가슴",
        imageName = "img_chest",
        colorHex = 0xFF0A88FF,
        colorDarkHex = 0xFF0A88FF,
    )
    val shoulder = ExerciseCategory(
        id = 1,
        name = "어깨",
        imageName = "img_shoulder",
        colorHex = 0xFF950AFF,
        colorDarkHex = 0xFF950AFF,
    )

    return listOf(
        WorkoutAddExerciseItem(
            exercise = Exercise(
                id = 1,
                name = "숄더 프레스 (바벨)",
                category = chest,
                imageName = chest.imageName,
            ),
            sets = listOf(
                WorkoutSet(index = 1, weightKg = 80.0, reps = 10),
                WorkoutSet(index = 2, weightKg = 85.0, reps = 10),
                WorkoutSet(index = 3, weightKg = 90.0, reps = 8),
            ),
        ),
        WorkoutAddExerciseItem(
            exercise = Exercise(
                id = 2,
                name = "밀리터리 프레스 (바벨)",
                category = shoulder,
                imageName = shoulder.imageName,
            ),
            sets = emptyList(),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutAddScreenPreview() {
    FitzamTheme {
        WorkoutAddScreen(
            selectedDate = LocalDate.now(),
            selectedExerciseIds = setOf(1L, 2L),
            onBackClick = {},
            onDetailAddClick = {},
            onCompleteClick = {},
        )
    }
}
