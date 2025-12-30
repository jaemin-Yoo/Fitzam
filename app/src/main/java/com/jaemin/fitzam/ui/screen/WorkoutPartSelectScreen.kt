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
import com.jaemin.fitzam.model.WorkoutPart
import com.jaemin.fitzam.ui.common.DZamButton
import com.jaemin.fitzam.ui.common.DZamOutlinedButton
import com.jaemin.fitzam.ui.common.FitzamTopAppBar
import com.jaemin.fitzam.ui.common.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme
import java.time.LocalDate

@Composable
fun WorkoutPartSelectScreen(
    selectedDate: LocalDate,
    onBackClick: () -> Unit,
    onPartClick: (WorkoutPart) -> Unit,
    onCompleteClick: () -> Unit,
    viewModel: WorkoutPartSelectViewModel = hiltViewModel(),
) {
    val parts by viewModel.workoutParts.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    WorkoutPartSelectScreen(
        parts = parts,
        selectedIds = selectedIds,
        onBackClick = onBackClick,
        onPartClick = { part ->
            viewModel.togglePart(part.id)
            onPartClick(part)
        },
        onCompleteClick = {
            viewModel.complete(selectedDate)
            onCompleteClick()
        },
    )
}

@Composable
fun WorkoutPartSelectScreen(
    parts: List<WorkoutPart>,
    selectedIds: Set<Long>,
    onBackClick: () -> Unit,
    onPartClick: (WorkoutPart) -> Unit,
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
            WorkoutPartGrid(
                parts = parts,
                selectedIds = selectedIds,
                onPartClick = { part ->
                    onPartClick(part)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 24.dp)
                ,
            )
            DZamOutlinedButton(
                text = "세부 운동 추가하기",
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth(),
                trailingIcon = ImageVector.vectorResource(R.drawable.ic_right_arrow),
            )
            Spacer(Modifier.height(32.dp))
            DZamButton(
                text = "완료",
                onClick = onCompleteClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WorkoutPartGrid(
    parts: List<WorkoutPart>,
    selectedIds: Set<Long>,
    onPartClick: (WorkoutPart) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = parts.size,
            key = { index -> parts[index].id },
        ) { index ->
            val part = parts[index]
            WorkoutPartGridItem(
                part = part,
                isSelected = selectedIds.contains(part.id),
                onClick = { onPartClick(part) },
            )
        }
    }
}

@Composable
private fun WorkoutPartGridItem(
    part: WorkoutPart,
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
                model = part.imageUrl,
                contentDescription = part.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .alpha(alpha = if (isSelected) 0.5f else 1f),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = part.name,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview
@Composable
fun WorkoutPartSelectScreenPreview() {
    FitzamTheme {
        WorkoutPartSelectScreen(
            parts = listOf(
                WorkoutPart(id = 0, name = "가슴", imageUrl = "", colorHex = "", colorDarkHex = ""),
                WorkoutPart(id = 1, name = "등", imageUrl = "", colorHex = "", colorDarkHex = ""),
                WorkoutPart(id = 2, name = "어깨", imageUrl = "", colorHex = "", colorDarkHex = ""),
                WorkoutPart(id = 3, name = "삼두", imageUrl = "", colorHex = "", colorDarkHex = ""),
            ),
            selectedIds = emptySet(),
            onBackClick = {},
            onPartClick = {},
            onCompleteClick = {},
        )
    }
}
