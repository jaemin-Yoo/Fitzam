package com.jaemin.fitzam.ui.screen.exerciseadd

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.model.ExerciseEquipmentType
import com.jaemin.fitzam.model.WorkoutMetricType
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.DZamInputField
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.IconSource
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.drawableResIdByName

@Composable
fun ExerciseAddScreen(
    selectedCategoryIds: Set<Long>,
    sessionId: Long,
    onBackClick: () -> Unit,
    onExerciseAdded: () -> Unit,
) {
    val viewModel: ExerciseAddViewModel = hiltViewModel(
        key = "exercise-add-$sessionId",
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val presetSuggestions by viewModel.presetSuggestions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var saveErrorVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedCategoryIds) {
        viewModel.loadCategories(selectedCategoryIds)
    }

    LaunchedEffect(saveErrorVersion) {
        if (saveErrorVersion > 0) {
            snackbarHostState.showSnackbar("운동 저장에 실패했습니다.")
        }
    }

    ExerciseAddScreen(
        uiState = uiState,
        isSaving = isSaving,
        presetSuggestions = presetSuggestions,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onExerciseNameChange = viewModel::onExerciseNameChange,
        onSuggestionSelected = { viewModel.clearPresetSuggestions() },
        onSaveClick = { name, category, equipmentType, metricTypes ->
            viewModel.addExercise(
                name = name,
                category = category,
                equipmentType = equipmentType,
                metricTypes = metricTypes,
                onSuccess = onExerciseAdded,
                onFailure = {
                    saveErrorVersion += 1
                },
            )
        },
    )
}

@Composable
fun ExerciseAddScreen(
    uiState: ExerciseAddUiState,
    isSaving: Boolean,
    presetSuggestions: List<Exercise>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onExerciseNameChange: (String) -> Unit,
    onSuggestionSelected: () -> Unit,
    onSaveClick: (String, ExerciseCategory, ExerciseEquipmentType, List<WorkoutMetricType>) -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 추가",
                navigation = TopAppBarItem(
                    icon = IconSource.Vector(ImageVector.vectorResource(R.drawable.ic_back)),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        when (uiState) {
            ExerciseAddUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            ExerciseAddUiState.Failed -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "운동 추가 정보를 불러오지 못했습니다.")
                }
            }

            is ExerciseAddUiState.Success -> {
                ExerciseAddContent(
                    categories = uiState.categories,
                    isSaving = isSaving,
                    presetSuggestions = presetSuggestions,
                    contentPadding = paddingValues,
                    onExerciseNameChange = onExerciseNameChange,
                    onSuggestionSelected = onSuggestionSelected,
                    onSaveClick = onSaveClick,
                )
            }
        }
    }
}

@Composable
private fun ExerciseAddContent(
    categories: List<ExerciseCategory>,
    isSaving: Boolean,
    presetSuggestions: List<Exercise>,
    contentPadding: PaddingValues,
    onExerciseNameChange: (String) -> Unit,
    onSuggestionSelected: () -> Unit,
    onSaveClick: (String, ExerciseCategory, ExerciseEquipmentType, List<WorkoutMetricType>) -> Unit,
) {
    var exerciseName by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable(categories) {
        mutableStateOf(categories.first().id)
    }
    var selectedEquipmentName by rememberSaveable {
        mutableStateOf(ExerciseEquipmentType.BARBELL.name)
    }
    var selectedMetricNames by rememberSaveable {
        mutableStateOf(
            setOf(
                WorkoutMetricType.WEIGHT_KG.name,
                WorkoutMetricType.REPS.name,
            )
        )
    }

    val selectedCategory = categories.firstOrNull { category -> category.id == selectedCategoryId }
        ?: categories.first()
    val selectedEquipmentType = ExerciseEquipmentType.valueOf(selectedEquipmentName)
    val selectedMetricTypes = selectedMetricNames
        .map { metricName -> WorkoutMetricType.valueOf(metricName) }
        .let { metricTypes ->
            MetricOptions.map { option -> WorkoutMetricType.valueOf(option.key) }
                .filter { metricType -> metricType in metricTypes }
        }
    val canSave = exerciseName.isNotBlank() && selectedMetricTypes.isNotEmpty() && !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        ExerciseImagePreview(category = selectedCategory)
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DZamInputField(
                value = exerciseName,
                onValueChange = { newName ->
                    exerciseName = newName
                    onExerciseNameChange(newName)
                },
                modifier = Modifier.fillMaxWidth(),
                label = "운동 이름",
                placeholder = "운동 이름",
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                ),
            )
            if (presetSuggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp)
                        .zIndex(1f)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    presetSuggestions.forEachIndexed { index, exercise ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    exerciseName = exercise.name
                                    selectedCategoryId = exercise.category.id
                                    selectedEquipmentName = exercise.equipmentType.name
                                    selectedMetricNames = exercise.metricTypes
                                        .map { it.name }
                                        .toSet()
                                    onSuggestionSelected()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            ExerciseCategoryTag(
                                name = exercise.category.name,
                                borderColor = Color(exercise.category.colorHex),
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        CategorySection(
            categories = categories,
            selectedCategoryId = selectedCategory.id,
            onCategoryClick = { category -> selectedCategoryId = category.id },
        )
        Spacer(modifier = Modifier.height(28.dp))
        ChipSection(
            title = "운동 도구",
            options = EquipmentOptions,
            selectedKeys = setOf(selectedEquipmentName),
            onOptionClick = { option -> selectedEquipmentName = option.key },
        )
        Spacer(modifier = Modifier.height(28.dp))
        ChipSection(
            title = "기록 유형",
            options = MetricOptions,
            selectedKeys = selectedMetricNames,
            onOptionClick = { option ->
                selectedMetricNames = if (selectedMetricNames.contains(option.key)) {
                    selectedMetricNames - option.key
                } else {
                    (selectedMetricNames + option.key).take(2).toSet()
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
        DZamButton(
            text = "완료",
            onClick = {
                onSaveClick(
                    exerciseName,
                    selectedCategory,
                    selectedEquipmentType,
                    selectedMetricTypes,
                )
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(
            modifier = Modifier.height(
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
            )
        )
    }
}

@Composable
private fun ExerciseImagePreview(
    category: ExerciseCategory,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            Image(
                painter = painterResource(drawableResIdByName(category.imageName)),
                contentDescription = category.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9D9D9))
                    .padding(10.dp),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9D9D9))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                    contentDescription = "이미지 편집",
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<ExerciseCategory>,
    selectedCategoryId: Long,
    onCategoryClick: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(text = "운동 유형")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                CategoryOption(
                    category = category,
                    selected = selectedCategoryId == category.id,
                    onClick = { onCategoryClick(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryOption(
    category: ExerciseCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(67.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(67.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Black,
                    shape = CircleShape,
                )
                .background(Color.White)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(drawableResIdByName(category.imageName)),
                contentDescription = category.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
                    .alpha(if (selected) 0.55f else 1f),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ChipSection(
    title: String,
    options: List<ExerciseAddOption>,
    selectedKeys: Set<String>,
    onOptionClick: (ExerciseAddOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionLabel(text = title)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                RoundOption(
                    option = option,
                    selected = selectedKeys.contains(option.key),
                    onClick = { onOptionClick(option) },
                )
            }
        }
    }
}

@Composable
private fun RoundOption(
    option: ExerciseAddOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(67.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(67.dp)
                .clip(CircleShape)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Black,
                    shape = CircleShape,
                )
                .background(Color.White)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(option.iconRes),
                contentDescription = option.label,
                tint = Color.Black,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth(),
    )
}

private data class ExerciseAddOption(
    val key: String,
    val label: String,
    @DrawableRes val iconRes: Int,
)

private val EquipmentOptions = listOf(
    ExerciseAddOption(
        key = ExerciseEquipmentType.BARBELL.name,
        label = ExerciseEquipmentType.BARBELL.displayName,
        iconRes = R.drawable.ic_barbell,
    ),
    ExerciseAddOption(
        key = ExerciseEquipmentType.DUMBBELL.name,
        label = ExerciseEquipmentType.DUMBBELL.displayName,
        iconRes = R.drawable.ic_dumbbell,
    ),
    ExerciseAddOption(
        key = ExerciseEquipmentType.KETTLEBELL.name,
        label = ExerciseEquipmentType.KETTLEBELL.displayName,
        iconRes = R.drawable.ic_kettlebell,
    ),
    ExerciseAddOption(
        key = ExerciseEquipmentType.MACHINE.name,
        label = ExerciseEquipmentType.MACHINE.displayName,
        iconRes = R.drawable.ic_machine,
    ),
    ExerciseAddOption(
        key = ExerciseEquipmentType.BODYWEIGHT.name,
        label = ExerciseEquipmentType.BODYWEIGHT.displayName,
        iconRes = R.drawable.ic_body_weight,
    ),
)

private val MetricOptions = listOf(
    ExerciseAddOption(
        key = WorkoutMetricType.WEIGHT_KG.name,
        label = "무게",
        iconRes = R.drawable.ic_kg,
    ),
    ExerciseAddOption(
        key = WorkoutMetricType.REPS.name,
        label = "횟수",
        iconRes = R.drawable.ic_count,
    ),
    ExerciseAddOption(
        key = WorkoutMetricType.DISTANCE_KM.name,
        label = "거리",
        iconRes = R.drawable.ic_distance,
    ),
    ExerciseAddOption(
        key = WorkoutMetricType.DURATION_SEC.name,
        label = "시간",
        iconRes = R.drawable.ic_time,
    ),
)

@Preview(showBackground = true)
@Composable
private fun ExerciseAddScreenPreview() {
    FitzamTheme {
        ExerciseAddScreen(
            uiState = ExerciseAddUiState.Success(
                categories = listOf(
                    ExerciseCategory(
                        id = 1,
                        name = "가슴",
                        imageName = "img_chest",
                        colorHex = 0xFF2563EB,
                        colorDarkHex = 0xFF2563EB,
                    ),
                    ExerciseCategory(
                        id = 2,
                        name = "등",
                        imageName = "img_back",
                        colorHex = 0xFF06B6D4,
                        colorDarkHex = 0xFF06B6D4,
                    ),
                    ExerciseCategory(
                        id = 3,
                        name = "어깨",
                        imageName = "img_shoulder",
                        colorHex = 0xFFD81DAF,
                        colorDarkHex = 0xFFD81DAF,
                    ),
                )
            ),
            isSaving = false,
            presetSuggestions = emptyList(),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onExerciseNameChange = {},
            onSuggestionSelected = {},
            onSaveClick = { _, _, _, _ -> },
        )
    }
}
