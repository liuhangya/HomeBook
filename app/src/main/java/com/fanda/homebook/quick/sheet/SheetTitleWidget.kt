package com.fanda.homebook.quick.sheet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

@Composable fun SheetTitleWidget(title: String, onSettingClick: (() -> Unit)? = null, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        if (onSettingClick != null){
            Box(modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    onSettingClick()
                }) {
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(20.dp), painter = painterResource(id = R.mipmap.icon_setting), contentDescription = null
                )
            }
        }

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

@Composable @Preview(showBackground = true) fun SheetTitleWidgetPreview() {
    SheetTitleWidget(title = "标题", onSettingClick = {}) {

    }
}