package com.jaemin.fitzam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.ui.common.DZamButton
import com.jaemin.fitzam.ui.common.DZamOutlinedButton
import com.jaemin.fitzam.ui.common.FitzamTopAppBar
import com.jaemin.fitzam.ui.common.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate

@Composable
fun ExerciseCategorySelectScreen(
    selectedDate: LocalDate,
    onBackClick: () -> Unit,
    onCategoryClick: (ExerciseCategory) -> Unit,
    onCompleteClick: () -> Unit,
    viewModel: ExerciseCategorySelectViewModel = hiltViewModel(),
) {
    val categories by viewModel.exerciseCategories.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    ExerciseCategorySelectScreen(
        categories = categories,
        selectedIds = selectedIds,
        onBackClick = onBackClick,
        onCategoryClick = { category ->
            viewModel.togglePart(category.id)
            onCategoryClick(category)
        },
        onCompleteClick = {
            viewModel.complete(selectedDate)
            onCompleteClick()
        },
    )
}

@Composable
fun ExerciseCategorySelectScreen(
    categories: List<ExerciseCategory>,
    selectedIds: Set<Long>,
    onBackClick: () -> Unit,
    onCategoryClick: (ExerciseCategory) -> Unit,
    onCompleteClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            FitzamTopAppBar(
                title = "운동 부위 선택",
                navigation = TopAppBarItem(
                    icon = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = "뒤로 가기",
                    onClick = onBackClick,
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp,
            )
        ) {
            ExerciseCategoryGrid(
                categories = categories,
                selectedIds = selectedIds,
                onCategoryClick = { category ->
                    onCategoryClick(category)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 24.dp),
            )
            DZamOutlinedButton(
                text = "세부 운동 추가하기",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIds.isNotEmpty(),
                trailingIcon = ImageVector.vectorResource(R.drawable.ic_right_arrow),
            )
            Spacer(Modifier.height(32.dp))
            DZamButton(
                text = "완료",
                onClick = onCompleteClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIds.isNotEmpty(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExerciseCategoryGrid(
    categories: List<ExerciseCategory>,
    selectedIds: Set<Long>,
    onCategoryClick: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = categories.size,
            key = { index -> categories[index].id },
        ) { index ->
            val category = categories[index]
            ExerciseCategoryGridItem(
                category = category,
                isSelected = selectedIds.contains(category.id),
                onClick = { onCategoryClick(category) },
            )
        }
    }
}

@Composable
private fun ExerciseCategoryGridItem(
    category: ExerciseCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                    shape = CircleShape,
                )
                .background(Color.White)
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .alpha(alpha = if (isSelected) 0.5f else 1f),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview
@Composable
fun ExerciseCategorySelectScreenPreview() {
    FitzamTheme {
        ExerciseCategorySelectScreen(
            categories = listOf(
                ExerciseCategory(
                    id = 0,
                    name = "가슴",
                    imageUrl = "",
                    colorHex = "",
                    colorDarkHex = ""
                ),
                ExerciseCategory(
                    id = 1,
                    name = "등",
                    imageUrl = "",
                    colorHex = "",
                    colorDarkHex = ""
                ),
                ExerciseCategory(
                    id = 2,
                    name = "어깨",
                    imageUrl = "",
                    colorHex = "",
                    colorDarkHex = ""
                ),
                ExerciseCategory(
                    id = 3,
                    name = "삼두",
                    imageUrl = "",
                    colorHex = "",
                    colorDarkHex = ""
                ),
            ),
            selectedIds = emptySet(),
            onBackClick = {},
            onCategoryClick = {},
            onCompleteClick = {},
        )
    }
}
