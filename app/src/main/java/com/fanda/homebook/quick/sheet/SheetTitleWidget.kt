package com.fanda.homebook.quick.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun SheetTitleWidget(title: String, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center), style = TextStyle.Default, text = title, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
        )
        TextButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp), onClick = onConfirm
        ) {
            Text(
                style = TextStyle.Default, text = "确定", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
            )
        }
    }
}