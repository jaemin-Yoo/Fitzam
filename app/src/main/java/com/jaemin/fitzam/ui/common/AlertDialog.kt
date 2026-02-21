package com.jaemin.fitzam.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jaemin.fitzam.ui.theme.FitzamTheme

/**
 * DZam 다이얼로그
 * Material 3 [Dialog] 래핑
 *
 * @param title 다이얼로그 제목 텍스트
 * @param text 다이얼로그 본문 텍스트
 * @param onConfirm 확인 버튼 클릭 시 호출되는 콜백
 * @param onCancel 취소 버튼 클릭 또는 다이얼로그 닫기 시 호출되는 콜백
 * @param modifier 다이얼로그 컨테이너 modifier
 * @param confirmText 확인 버튼 텍스트
 * @param cancelText 취소 버튼 텍스트
 * @param onDismissRequest 다이얼로그 외부 클릭/뒤로가기 시 호출되는 콜백
 */
@Composable
fun DZamAlertDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "확인",
    cancelText: String = "취소",
    onDismissRequest: () -> Unit = onCancel,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier
                .widthIn(min = 280.dp, max = 360.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentColor = Color.White,
                        )
                    ) {
                        Text(text = cancelText)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    ) {
                        Text(text = confirmText)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DZamModalDialogPreview() {
    FitzamTheme {
        DZamAlertDialog(
            title = "제목",
            text = "내용",
            onConfirm = {},
            onCancel = {},
        )
    }
}
