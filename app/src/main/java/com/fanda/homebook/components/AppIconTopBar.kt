package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

@Composable fun TopIconAppBar(
    title: String, modifier: Modifier = Modifier, showBackButton: Boolean = true, backIconPainter: Painter = painterResource(id = R.mipmap.icon_back), onBackClick: (() -> Unit)? = null,

    rightText: String? = null, rightIconPainter: Painter? = null, rightNextIconPainter: Painter? = null, onRightActionClick: (() -> Unit)? = null, onRightNextActionClick: (() -> Unit)? = null,
    // 样式
    titleStyle: TextStyle = TextStyle.Default.copy(
        fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium
    ), backgroundColor: Color = Color.Transparent, contentColor: Color = MaterialTheme.colorScheme.onSurface
) {

    Box(
        modifier = modifier
            .height(64.dp)
            .background(color = backgroundColor)
            .padding(start = 12.dp, end = 12.dp)
            .fillMaxWidth()
    ) {
        // ← 左侧：返回按钮（Image 图标）
        if (showBackButton) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .align(Alignment.CenterStart)
                .clickable(enabled = onBackClick != null) {
                    onBackClick?.invoke()
                }) {
                Image(
                    painter = backIconPainter, contentDescription = "Back", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                )
            }
        }
        // ↑ 居中标题
        Text(
            text = title, style = titleStyle, color = contentColor, maxLines = 1, modifier = Modifier.align(Alignment.Center)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (rightIconPainter != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(44.dp)
                    .then(if (onRightActionClick != null) {
                        Modifier.clickable { onRightActionClick() }
                    } else Modifier)) {
                    Image(
                        painter = rightIconPainter, contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (rightNextIconPainter != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(44.dp)
                    .then(if (onRightNextActionClick != null) {
                        Modifier.clickable { onRightNextActionClick() }
                    } else Modifier)) {
                    Image(
                        painter = rightNextIconPainter, contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (!rightText.isNullOrBlank()) {
                TextButton(onClick = { onRightActionClick?.invoke() }) {
                    Text(
                        text = rightText, style = TextStyle.Default.copy(
                            fontSize = 16.sp, color = colorResource(id = R.color.color_333333)
                        ), modifier = if (onRightActionClick != null) {
                            Modifier
                        } else {
                            Modifier
                        }
                    )
                }
            }
        }
    }
}

@Composable @Preview(showBackground = true) private fun CustomTopAppBarPreview() {
    TopIconAppBar(
        title = "记一笔",
        onBackClick = {},
        rightIconPainter = painterResource(R.mipmap.icon_add_grady),
        rightNextIconPainter = painterResource(R.mipmap.icon_edit_menu),
        onRightActionClick = {},
        backIconPainter = painterResource(R.mipmap.icon_back),
    )
}