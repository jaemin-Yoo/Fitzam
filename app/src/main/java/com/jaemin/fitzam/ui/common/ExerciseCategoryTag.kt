package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.theme.FitzamTheme

@Composable
fun ExerciseCategoryTag(
    name: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                vertical = 4.dp,
                horizontal = 16.dp
            )
        )
    }
}

@Preview
@Composable
fun ExerciseCategoryPreview() {
    FitzamTheme {
        ExerciseCategoryTag(
            name = "테스트",
            borderColor = Color.Red,
        )
    }
}