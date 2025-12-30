package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable fun CustomTopAppBar(
    title: String, modifier: Modifier = Modifier,
    // ← 左侧：返回图标（可选）
    showBackButton: Boolean = true, backIconPainter: Painter? = null, onBackClick: (() -> Unit)? = null,

    // → 右侧：支持文本 或 Image 图标（二选一，优先显示图标）
    rightText: String? = null, rightIconPainter: Painter? = null, onRightActionClick: (() -> Unit)? = null,

    // 样式
    titleStyle: TextStyle = TextStyle.Default.copy(
        fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium
    ), backgroundColor: Color = Color.Transparent, contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        color = backgroundColor, modifier = modifier.padding(horizontal = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            // ← 左侧：返回按钮（Image 图标）
            if (showBackButton) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(enabled = onBackClick != null) {
                        onBackClick?.invoke()
                    }) {
                    if (backIconPainter != null) {
                        Image(
                            painter = backIconPainter, contentDescription = "Back", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // 默认 fallback 到系统箭头（可选）
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentColor, modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            // ↑ 居中标题
            Text(
                text = title, style = titleStyle, color = contentColor, maxLines = 1
            )

            // → 右侧：优先显示图标，其次文本
            if (rightIconPainter != null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(48.dp)
                    .then(if (onRightActionClick != null) {
                        Modifier.clickable { onRightActionClick() }
                    } else Modifier)) {
                    Image(
                        painter = rightIconPainter, contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                    )
                }
            } else if (!rightText.isNullOrBlank()) {
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
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable @Preview(showBackground = true) private fun CustomTopAppBarPreview() {
    CustomTopAppBar(
        title = "记一笔",
        onBackClick = {},
        rightText = "保存",
        onRightActionClick = {},
        backIconPainter = painterResource(R.mipmap.icon_back),
    )
}