package com.fanda.homebook.common.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 重命名或删除操作类型枚举
 */
enum class RenameOrDeleteType {
    RENAME,  // 重命名操作
    DELETE   // 删除操作
}

/**
 * 重命名或删除底部弹窗组件
 *
 * 提供一个底部弹窗，包含重命名和删除两个操作选项
 *
 * @param modifier 修饰符，用于自定义弹窗内容区域的布局
 * @param firstMenuText 第一个菜单项的文本，默认为"重命名"
 * @param secondMenuText 第二个菜单项的文本，默认为"删除"
 * @param visible 控制弹窗的显示/隐藏状态
 * @param onDismiss 弹窗关闭回调函数
 * @param onConfirm 菜单项点击确认回调，返回选择的操作类型
 */
@Composable fun RenameOrDeleteBottomSheet(
    modifier: Modifier = Modifier, firstMenuText: String = "重命名", secondMenuText: String = "删除", visible: Boolean, onDismiss: () -> Unit, onConfirm: (RenameOrDeleteType) -> Unit
) {
    CustomBottomSheet(
        visible = visible, onDismiss = onDismiss
    ) {
        Column(
            modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中对齐
            verticalArrangement = Arrangement.Center             // 垂直居中对齐
        ) {
            // 第一个菜单项：重命名
            Text(
                text = firstMenuText, fontSize = 16.sp, color = Color.Black,                             // 黑色文本
                textAlign = TextAlign.Center,                    // 居中对齐
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onConfirm(RenameOrDeleteType.RENAME)    // 点击触发重命名回调
                    }
                    .padding(vertical = 26.dp)                   // 垂直内边距
            )

            // 分割线1
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), // 浅灰色分割线
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)                 // 水平内边距
            )

            // 第二个菜单项：删除
            Text(
                text = secondMenuText, fontSize = 16.sp, color = colorResource(id = R.color.color_FF2822), // 红色文本（警示色）
                fontWeight = FontWeight.Medium,                   // 中等字重
                textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onConfirm(RenameOrDeleteType.DELETE)     // 点击触发删除回调
                    }
                    .padding(vertical = 26.dp))

            // 分割线2
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            // 取消按钮区域
            Box(
                modifier = Modifier.padding(24.dp)              // 四周内边距
            ) {
                SelectableRoundedButton(
                    text = "取消", selected = false,                            // 非选中状态
                    onClick = onDismiss,                         // 点击关闭弹窗
                    cornerSize = 27.dp,                          // 圆角大小
                    modifier = Modifier.fillMaxWidth(),          // 填充宽度
                    contentPadding = PaddingValues(vertical = 16.dp), // 垂直内边距
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览重命名/删除底部弹窗
 */
@Composable @Preview(showBackground = true) fun RenameOrDeleteBottomSheetPreview() {
    HomeBookTheme {
        RenameOrDeleteBottomSheet(visible = true, onDismiss = {}, onConfirm = {})
    }
}