package com.jaemin.fitzam.ui.dzam

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jaemin.fitzam.ui.theme.FitzamTheme

/**
 * DZam 입력창. Material 3 [OutlinedTextField] 래핑
 *
 * @param value 입력창에 표시되는 텍스트
 * @param onValueChange 텍스트 변경 시 호출되는 콜백
 * @param modifier 입력창 modifier
 * @param enabled 입력창 활성화 여부
 * @param label 입력창 위에 표시되는 텍스트
 * @param placeholder 입력창 힌트
 * @param leadingIcon 입력창 시작 아이콘
 * @param trailingIcon 입력창 끝 아이콘
 * @param supportingText 입력창 밑에 표시되는 텍스트
 * @param isError 입력창에 에러 상태 전달. 입력창 테두리가 빨간 색으로 변경됨
 * @param keyboardOptions 입력창 키보드 옵션. KeyboardType, ImeAction 등
 * @param keyboardActions 입력창 키보드 액션. Done, Next, Search 등
 */
@Composable
fun DZamInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String = "",
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text(text = placeholder)
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText.takeIf { it.isNotBlank() }?.let { text ->
                {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            },
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = colors,
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DZamInputFieldPreview() {
    FitzamTheme {
        DZamInputField(
            value = "",
            onValueChange = {},
            label = "라벨",
            placeholder = "텍스트 입력",
            supportingText = "보조 텍스트",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
        )
    }
}
