package com.jaemin.fitzam.ui.screen.exercisecategoryselect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaemin.fitzam.model.ExerciseCategory
import com.jaemin.fitzam.ui.util.drawableResIdByName
import com.jaemin.fitzam.ui.dzam.DZamAlertDialog
import com.jaemin.fitzam.ui.dzam.DZamButton
import java.time.LocalDate

@Composable
fun ExerciseCategorySelectBottomSheet(
    selectedDate: LocalDate,
    sessionId: Long,
    onCompleteClick: () -> Unit,
) {
    val viewModel: ExerciseCategorySelectViewModel = hiltViewModel(
        key = "exercise-category-select-$sessionId",
    )
    val uiState by viewModel.exerciseCategorySelectUiState.collectAsStateWithLifecycle()
    val selectedCategoryIds by viewModel.selectedCategoryIds.collectAsStateWithLifecycle()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.loadSelectedCategories(selectedDate)
    }

    LaunchedEffect(Unit) {
        viewModel.completeEvents.collect {
            onCompleteClick()
        }
    }

    val currentUiState = uiState
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "운동 유형 선택",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
        )
        when (currentUiState) {
            ExerciseCategorySelectUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            ExerciseCategorySelectUiState.Failed -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "로딩에 실패했습니다. 다시 시도해 주세요.")
                }
            }
            is ExerciseCategorySelectUiState.Success -> {
                val categories = currentUiState.exerciseCategories
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    categories.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            rowItems.forEach { category ->
                                ExerciseCategoryGridItem(
                                    category = category,
                                    isSelected = selectedCategoryIds.contains(category.id),
                                    onClick = { viewModel.toggleCategory(category.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        if (currentUiState is ExerciseCategorySelectUiState.Success) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                DZamButton(
                    text = "완료",
                    onClick = { viewModel.requestComplete(selectedDate) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DZamAlertDialog(
            title = "정말 삭제하시겠어요?",
            text = "이미 추가된 운동이 있습니다.\n계속하시겠습니까?",
            onConfirm = { viewModel.confirmDelete() },
            onCancel = { viewModel.dismissDeleteConfirmation() },
            confirmText = "삭제",
            cancelText = "취소",
        )
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
            Image(
                painter = painterResource(drawableResIdByName(category.imageName)),
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
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
