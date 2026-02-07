package com.fanda.homebook.common.sheet

import androidx.compose.foundation.Image
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

/**
 * 底部弹窗标题栏组件
 * 用于所有底部弹窗的统一标题栏，包含左侧设置按钮、中间标题和右侧确认按钮
 *
 * @param title 弹窗标题文本
 * @param onSettingClick 设置按钮点击回调函数（可选，为空时不显示设置按钮）
 * @param onConfirm 确认按钮点击回调函数
 */
@Composable fun SheetTitleWidget(
    title: String, onSettingClick: (() -> Unit)? = null, onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)  // 顶部内边距
    ) {
        // 左侧：设置按钮（可选）
        if (onSettingClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)  // 左对齐
                    .padding(start = 14.dp)
                    .size(40.dp)  // 固定大小
                    .clip(CircleShape)  // 圆形裁剪
                    .clickable {
                        onSettingClick()  // 点击触发设置回调
                    }) {
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)  // 图片居中
                        .size(20.dp),  // 图片大小
                    painter = painterResource(id = R.mipmap.icon_setting), contentDescription = "设置"  // 无障碍描述
                )
            }
        }

        // 中间：标题文本
        Text(
            modifier = Modifier.align(Alignment.Center),  // 居中对齐
            style = TextStyle.Default, text = title, color = Color.Black, fontWeight = FontWeight.Medium,  // 中等粗细
            fontSize = 16.sp
        )

        // 右侧：确认按钮
        TextButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)  // 右对齐
                .padding(end = 10.dp), onClick = onConfirm  // 点击触发确认回调
        ) {
            Text(
                style = TextStyle.Default, text = "确定", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
            )
        }
    }
}

/**
 * 预览函数 - 用于Android Studio的Compose预览
 *
 * @see SheetTitleWidget 查看完整参数说明
 */
@Composable @Preview(showBackground = true) fun SheetTitleWidgetPreview() {
    SheetTitleWidget(
        title = "标题", onSettingClick = {
            // 预览用的空回调
        }) {
        // 预览用的空回调
    }
}