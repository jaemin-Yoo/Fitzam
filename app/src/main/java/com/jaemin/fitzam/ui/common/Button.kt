package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * DZam 버튼
 *
 * @param text 버튼 텍스트 내용
 * @param onClick 버튼 클릭 시 호출되는 콜백
 * @param modifier 버튼 modifier
 * @param enabled 버튼 활성화 여부
 * @param colors 버튼 색상
 * @param leadingIcon 버튼 왼쪽 아이콘. 'null' 이면 아이콘 없음
 * @param trailingIcon 버튼 오른쪽 아이콘. 'null' 이면 아이콘 없음
 */
@Composable
fun DZamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
    ),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val appliedModifier = if (enabled) modifier else modifier.alpha(0.3f)
    Button(
        onClick = onClick,
        modifier = appliedModifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = colors,
        contentPadding = PaddingValues(16.dp)
    ) {
        if (leadingIcon != null) {
            Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                )
            }
        }
        Box(
            modifier = Modifier.padding(
                start = if (leadingIcon != null) ButtonDefaults.IconSpacing else 0.dp,
                end = if (trailingIcon != null) ButtonDefaults.IconSpacing else 0.dp,
            ),
        ) {
            Text(text = text)
        }
        if (trailingIcon != null) {
            Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                )
            }
        }
    }
}

/**
 * DZam 테두리 버튼
 *
 * @param text 버튼 텍스트 내용
 * @param onClick 버튼 클릭 시 호출되는 콜백
 * @param modifier 버튼 modifier
 * @param enabled 버튼 활성화 여부
 * @param colors 버튼 색상
 * @param leadingIcon 버튼 왼쪽 아이콘. 'null' 이면 아이콘 없음
 * @param trailingIcon 버튼 오른쪽 아이콘. 'null' 이면 아이콘 없음
 */
@Composable
fun DZamOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        disabledContentColor = MaterialTheme.colorScheme.primary,
    ),
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val appliedModifier = if (enabled) modifier else modifier.alpha(0.3f)
    OutlinedButton(
        onClick = onClick,
        modifier = appliedModifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (leadingIcon != null) {
            Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                )
            }
        }
        Box(
            modifier = Modifier.padding(
                start = if (leadingIcon != null) ButtonDefaults.IconSpacing else 0.dp,
                end = if (trailingIcon != null) ButtonDefaults.IconSpacing else 0.dp,
            ),
        ) {
            Text(text = text)
        }
        if (trailingIcon != null) {
            Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                )
            }
        }
    }
}
