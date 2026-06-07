package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.model.Exercise
import com.jaemin.fitzam.ui.util.drawableResIdByName

@Composable
fun ExerciseInfoRow(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    imageSize: Dp = 52.dp,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cardShape = RoundedCornerShape(12.dp)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(imageSize)
                .clip(cardShape)
                .background(Color(exercise.category.colorHex).copy(alpha = 0.2f)),
        ) {
            Image(
                painter = painterResource(drawableResIdByName(exercise.imageName)),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(imageSize * 0.6f),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ExerciseCategoryTag(
                name = exercise.category.name,
                borderColor = Color(exercise.category.colorHex),
            )
        }
    }
}
