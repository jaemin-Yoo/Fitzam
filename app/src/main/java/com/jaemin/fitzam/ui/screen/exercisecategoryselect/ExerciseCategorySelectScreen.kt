package com.jaemin.fitzam.ui.screen.exercisecategoryselect

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    val cornerShape = RoundedCornerShape(12.dp)
    val imageCornerShape = RoundedCornerShape(12.dp)
    val categoryColor = Color(category.colorHex)
    val defaultContentColor = LocalContentColor.current
    val animDuration = 250

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(animDuration),
        label = "borderWidth",
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor else Color.Transparent,
        animationSpec = tween(animDuration),
        label = "borderColor",
    )
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor.copy(alpha = 0.12f)
                      else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
        animationSpec = tween(animDuration),
        label = "backgroundColor",
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor else defaultContentColor,
        animationSpec = tween(animDuration),
        label = "textColor",
    )
    val animatedShadowAmbientColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor.copy(alpha = 0.3f) else Color.Black,
        animationSpec = tween(animDuration),
        label = "shadowAmbientColor",
    )
    val animatedShadowSpotColor by animateColorAsState(
        targetValue = if (isSelected) categoryColor.copy(alpha = 0.5f) else Color.Black,
        animationSpec = tween(animDuration),
        label = "shadowSpotColor",
    )
    val animatedImageAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.5f else 1f,
        animationSpec = tween(animDuration),
        label = "imageAlpha",
    )
    val animatedCheckAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(animDuration),
        label = "checkAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cornerShape)
            .border(
                width = animatedBorderWidth,
                color = animatedBorderColor,
                shape = cornerShape,
            )
            .background(animatedBackgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = imageCornerShape,
                        ambientColor = animatedShadowAmbientColor,
                        spotColor = animatedShadowSpotColor,
                    )
                    .clip(imageCornerShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(drawableResIdByName(category.imageName)),
                    contentDescription = category.name,
                    modifier = Modifier
                        .size(36.dp)
                        .alpha(animatedImageAlpha),
                    contentScale = ContentScale.Fit,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .alpha(animatedCheckAlpha),
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(categoryColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = animatedTextColor,
        )
    }
}
