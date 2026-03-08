package com.jaemin.fitzam.ui.screen.workoutadd

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.DZamOutlinedButton
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.theme.SuccessGreen
import com.jaemin.fitzam.ui.util.drawableResIdByName
import java.time.LocalDate

private data class WorkoutAddExerciseUiState(
    val exercise: Exercise,
    val sets: List<EditableWorkoutSetUi>,
    val isEditing: Boolean = false,
)

@Composable
fun WorkoutAddScreen(
    selectedDate: LocalDate,
    selectedExerciseIds: Set<Long>,
    sessionId: Long,
    onBackClick: () -> Unit,
    onDetailAddClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onExerciseStartClick: (Exercise) -> Unit,
) {
    val viewModel: WorkoutAddViewModel = hiltViewModel(
        key = "workout-add-$sessionId",
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exerciseItemsFromViewModel by viewModel.exerciseItems.collectAsStateWithLifecycle()

    LaunchedEffect(selectedExerciseIds) {
        viewModel.loadExercises(selectedExerciseIds)
    }

    when (uiState) {
        WorkoutAddUiState.Loading -> {
            WorkoutAddLoadingScreen(onBackClick = onBackClick)
        }

        WorkoutAddUiState.Failed -> {
            WorkoutAddFailedScreen(onBackClick = onBackClick)
        }

        is WorkoutAddUiState.Success -> {
            var editingExerciseIds by remember {
                mutableStateOf(emptySet<Long>())
            }
            val availableIds = exerciseItemsFromViewModel.map { item -> item.exercise.id }.toSet()
            editingExerciseIds = editingExerciseIds.intersect(availableIds)
            val exerciseItems = exerciseItemsFromViewModel.map { item ->
                WorkoutAddExerciseUiState(
                    exercise = item.exercise,
                    sets = item.sets,
                    isEditing = editingExerciseIds.contains(item.exercise.id),
                )
            }

            WorkoutAddContent(
                selectedDate = selectedDate,
                exerciseItems = exerciseItems,
                onBackClick = onBackClick,
                onDetailAddClick = onDetailAddClick,
                onCompleteClick = onCompleteClick,
                onExerciseToggleEditClick = { exerciseId ->
                    editingExerciseIds = if (editingExerciseIds.contains(exerciseId)) {
                        editingExerciseIds - exerciseId
                    } else {
                        editingExerciseIds + exerciseId
                    }
                },
                onExerciseStartClick = { exercise ->
                    onExerciseStartClick(exercise)
                },
                onExerciseDeleteClick = { exerciseId ->
                    editingExerciseIds = editingExerciseIds - exerciseId
                    viewModel.deleteExercise(exerciseId)
                },
                onSetWeightChange = { exerciseId, setIndex, weight ->
                    viewModel.updateSetWeight(exerciseId, setIndex, weight)
                },
                onSetRepsChange = { exerciseId, setIndex, reps ->
                    viewModel.updateSetReps(exerciseId, setIndex, reps)
                },
                onSetDeleteClick = { exerciseId, setIndex ->
                    viewModel.deleteSet(exerciseId, setIndex)
                },
            )
        }
    }
}

@Composable
private fun WorkoutAddContent(
    selectedDate: LocalDate,
    exerciseItems: List<WorkoutAddExerciseUiState>,
    onBackClick: () -> Unit,
    onDetailAddClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onExerciseToggleEditClick: (Long) -> Unit,
    onExerciseStartClick: (Exercise) -> Unit,
    onExerciseDeleteClick: (Long) -> Unit,
    onSetWeightChange: (Long, Int, String) -> Unit,
    onSetRepsChange: (Long, Int, String) -> Unit,
    onSetDeleteClick: (Long, Int) -> Unit,
) {
    val selectedCategories = exerciseItems
        .map { item -> item.exercise.category }
        .distinctBy { category -> category.id }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 기록",
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
                    text = "운동 유형",
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedCategories.isEmpty()) {
                    Text(
                        text = "선택한 운동이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedCategories.forEach { category ->
                            ExerciseCategoryTag(
                                name = category.name,
                                borderColor = Color(category.colorHex),
                            )
                        }
                    }
                }
            }

            items(
                items = exerciseItems,
                key = { it.exercise.id },
            ) { item ->
                WorkoutExerciseCard(
                    exerciseItem = item,
                    onEditClick = { onExerciseToggleEditClick(item.exercise.id) },
                    onStartClick = { onExerciseStartClick(item.exercise) },
                    onDeleteClick = { onExerciseDeleteClick(item.exercise.id) },
                    onSetWeightChange = { setIndex, weight ->
                        onSetWeightChange(item.exercise.id, setIndex, weight)
                    },
                    onSetRepsChange = { setIndex, reps ->
                        onSetRepsChange(item.exercise.id, setIndex, reps)
                    },
                    onSetDeleteClick = { setIndex ->
                        onSetDeleteClick(item.exercise.id, setIndex)
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
private fun WorkoutAddLoadingScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 기록",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                ),
            )
        },
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

@Composable
private fun WorkoutAddFailedScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 기록",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "운동 목록 로딩에 실패했습니다.")
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
                    containerColor = MaterialTheme.colorScheme.surface,
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

private fun sampleWorkoutAddItems(): List<WorkoutAddExerciseUiModel> {
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
        WorkoutAddExerciseUiModel(
            exercise = Exercise(
                id = 1,
                name = "숄더 프레스 (바벨)",
                category = chest,
                imageName = chest.imageName,
            ),
            sets = listOf(
                EditableWorkoutSetUi(index = 1, weightText = "80", repsText = "10"),
                EditableWorkoutSetUi(index = 2, weightText = "85", repsText = "10"),
                EditableWorkoutSetUi(index = 3, weightText = "90", repsText = "8"),
            ),
        ),
        WorkoutAddExerciseUiModel(
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
        WorkoutAddContent(
            selectedDate = LocalDate.now(),
            exerciseItems = sampleWorkoutAddItems().map { item ->
                WorkoutAddExerciseUiState(
                    exercise = item.exercise,
                    sets = item.sets,
                )
            },
            onBackClick = {},
            onDetailAddClick = {},
            onCompleteClick = {},
            onExerciseToggleEditClick = {},
            onExerciseStartClick = {},
            onExerciseDeleteClick = {},
            onSetWeightChange = { _, _, _ -> },
            onSetRepsChange = { _, _, _ -> },
            onSetDeleteClick = { _, _ -> },
        )
    }
}
