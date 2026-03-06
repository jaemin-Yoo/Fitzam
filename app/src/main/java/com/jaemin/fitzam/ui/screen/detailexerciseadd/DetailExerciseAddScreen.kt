package com.jaemin.fitzam.ui.screen.detailexerciseadd

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.ui.common.ExerciseCategoryTag
import com.jaemin.fitzam.ui.dzam.DZamButton
import com.jaemin.fitzam.ui.dzam.DZamInputField
import com.jaemin.fitzam.ui.dzam.FitzamTopAppBar
import com.jaemin.fitzam.ui.dzam.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import com.jaemin.fitzam.ui.util.drawableResIdByName
import java.time.LocalDate

@Composable
fun DetailExerciseAddScreen(
    selectedDate: LocalDate,
    selectedCategoryIds: Set<Long>,
    initialSelectedIds: Set<Long>,
    onBackClick: () -> Unit,
    onCompleteClick: (Set<Long>) -> Unit,
    viewModel: DetailExerciseAddViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedExerciseIds by viewModel.selectedExerciseIds.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialSelectedIds) {
        viewModel.setInitialSelectedExerciseIds(initialSelectedIds)
    }

    LaunchedEffect(selectedCategoryIds) {
        viewModel.loadExercises(selectedCategoryIds)
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            if (event is DetailExerciseAddEvent.FavoriteSaveFailed) {
                snackbarHostState.showSnackbar("즐겨찾기 저장에 실패했습니다.")
            }
        }
    }

    when (val value = uiState) {
        DetailExerciseAddUiState.Loading -> {
            DetailExerciseAddLoadingScreen(onBackClick = onBackClick)
        }
        DetailExerciseAddUiState.Failed -> {
            DetailExerciseAddFailedScreen(onBackClick = onBackClick)
        }
        is DetailExerciseAddUiState.Success -> {
            DetailExerciseAddScreen(
                selectedDate = selectedDate,
                onBackClick = onBackClick,
                onCompleteClick = onCompleteClick,
                exercises = value.exercises,
                selectedExerciseIds = selectedExerciseIds,
                onToggleSelected = viewModel::toggleSelectedExercise,
                favoriteExerciseIds = value.favoriteIds,
                onToggleFavorite = viewModel::toggleFavorite,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@Composable
fun DetailExerciseAddScreen(
    selectedDate: LocalDate,
    onBackClick: () -> Unit,
    onCompleteClick: (Set<Long>) -> Unit,
    exercises: List<Exercise>,
    selectedExerciseIds: Set<Long>,
    onToggleSelected: (Long) -> Unit,
    favoriteExerciseIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredExercises = exercises.filter { exercise ->
        exercise.name.contains(searchQuery, ignoreCase = true)
    }
    val favoriteExercises = filteredExercises.filter { exercise ->
        favoriteExerciseIds.contains(exercise.id)
    }

    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "세부 운동 추가",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
                    )
            ) {
                DZamButton(
                    text = "${selectedExerciseIds.size}개 선택 완료",
                    onClick = { onCompleteClick(selectedExerciseIds) },
                    enabled = selectedExerciseIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                )
        ) {
            DZamInputField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                placeholder = "운동을 검색하세요.",
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "검색",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            DetailExerciseAddList(
                favoriteExercises = favoriteExercises,
                exercises = filteredExercises,
                selectedExerciseIds = selectedExerciseIds,
                favoriteExerciseIds = favoriteExerciseIds,
                onToggleSelected = onToggleSelected,
                onToggleFavorite = onToggleFavorite,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DetailExerciseAddLoadingScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "세부 운동 추가",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
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
private fun DetailExerciseAddFailedScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "세부 운동 추가",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
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
private fun DetailExerciseAddList(
    favoriteExercises: List<Exercise>,
    exercises: List<Exercise>,
    selectedExerciseIds: Set<Long>,
    favoriteExerciseIds: Set<Long>,
    onToggleSelected: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (favoriteExercises.isNotEmpty()) {
            item {
                SectionTitle(text = "즐겨찾는 운동")
            }
            items(
                items = favoriteExercises,
                key = { "favorite-${it.id}" },
            ) { exercise ->
                DetailExerciseAddItem(
                    exercise = exercise,
                    isSelected = selectedExerciseIds.contains(exercise.id),
                    isFavorite = favoriteExerciseIds.contains(exercise.id),
                    onToggleSelected = { onToggleSelected(exercise.id) },
                    onToggleFavorite = { onToggleFavorite(exercise.id) },
                )
            }
        }

        item {
            SectionTitle(text = "전체 보기")
        }
        items(
            items = exercises,
            key = { "all-${it.id}" },
        ) { exercise ->
            DetailExerciseAddItem(
                exercise = exercise,
                isSelected = selectedExerciseIds.contains(exercise.id),
                isFavorite = favoriteExerciseIds.contains(exercise.id),
                onToggleSelected = { onToggleSelected(exercise.id) },
                onToggleFavorite = { onToggleFavorite(exercise.id) },
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun DetailExerciseAddItem(
    exercise: Exercise,
    isSelected: Boolean,
    isFavorite: Boolean,
    onToggleSelected: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelected),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Image(
            painter = painterResource(drawableResIdByName(exercise.imageName)),
            contentDescription = exercise.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = exercise.name)
            ExerciseCategoryTag(
                name = exercise.category.name,
                borderColor = Color(exercise.category.colorHex),
            )
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                painter = if (isFavorite) painterResource(R.drawable.ic_filled_star) else painterResource(R.drawable.ic_outline_star),
                contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val sampleFavoriteIds = setOf(0L, 1L)

private fun sampleExercises(): List<Exercise> {
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
    val lowerBody = ExerciseCategory(
        id = 2,
        name = "하체",
        imageName = "img_lower_body",
        colorHex = 0xFF3FA400,
        colorDarkHex = 0xFF3FA400,
    )

    return listOf(
        Exercise(
            id = 0,
            name = "벤치 프레스 (머신)",
            category = chest,
            imageName = chest.imageName,
        ),
        Exercise(
            id = 1,
            name = "숄더 프레스 (바벨)",
            category = shoulder,
            imageName = shoulder.imageName,
        ),
        Exercise(
            id = 2,
            name = "레그 프레스 (머신)",
            category = lowerBody,
            imageName = lowerBody.imageName,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun DetailExerciseAddScreenPreview() {
    FitzamTheme {
        DetailExerciseAddScreen(
            selectedDate = LocalDate.now(),
            onBackClick = {},
            onCompleteClick = {},
            exercises = sampleExercises(),
            selectedExerciseIds = setOf(1L, 2L),
            onToggleSelected = {},
            onToggleFavorite = {},
            snackbarHostState = remember { SnackbarHostState() },
            favoriteExerciseIds = sampleFavoriteIds,
        )
    }
}


