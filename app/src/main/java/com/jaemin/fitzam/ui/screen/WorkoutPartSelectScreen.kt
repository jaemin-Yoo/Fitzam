package com.jaemin.fitzam.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.jaemin.fitzam.R
import com.jaemin.fitzam.model.WorkoutPart
import com.jaemin.fitzam.ui.common.FitzamTopAppBar
import com.jaemin.fitzam.ui.common.TopAppBarItem
import com.jaemin.fitzam.ui.theme.FitzamTheme

@Composable
fun WorkoutPartSelectScreen(
    onBackClick: () -> Unit,
    viewModel: WorkoutPartSelectViewModel = hiltViewModel(),
    onPartClick: (WorkoutPart) -> Unit = {},
) {
    val parts by viewModel.workoutParts.collectAsState()
    WorkoutPartSelectScreen(
        parts = parts,
        onBackClick = onBackClick,
        onPartClick = onPartClick,
    )
}

@Composable
fun WorkoutPartSelectScreen(
    parts: List<WorkoutPart>,
    onBackClick: () -> Unit,
    onPartClick: (WorkoutPart) -> Unit = {},
) {
    val selectedCodesSaver = listSaver<Set<String>, String>(
        save = { it.toList() },
        restore = { it.toSet() },
    )
    var selectedCodes by rememberSaveable(stateSaver = selectedCodesSaver) {
        mutableStateOf(emptySet())
    }

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
        }
    ) { paddingValues ->
        WorkoutPartGrid(
            parts = parts,
            selectedCodes = selectedCodes,
            onPartClick = { part ->
                selectedCodes = if (selectedCodes.contains(part.code)) {
                    selectedCodes - part.code
                } else {
                    selectedCodes + part.code
                }
                onPartClick(part)
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun WorkoutPartGrid(
    parts: List<WorkoutPart>,
    selectedCodes: Set<String>,
    onPartClick: (WorkoutPart) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = parts.size,
            key = { index -> parts[index].code },
        ) { index ->
            val part = parts[index]
            WorkoutPartGridItem(
                part = part,
                isSelected = selectedCodes.contains(part.code),
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
                contentDescription = part.displayName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .alpha(alpha = if (isSelected) 0.5f else 1f),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = part.displayName,
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
                WorkoutPart(code = "CHEST", displayName = "가슴", imageUrl = ""),
                WorkoutPart(code = "BACK", displayName = "등", imageUrl = ""),
                WorkoutPart(code = "SHOULDER", displayName = "어깨", imageUrl = ""),
                WorkoutPart(code = "TRICEPS", displayName = "삼두", imageUrl = ""),
            ),
            onBackClick = {},
        )
    }
}
