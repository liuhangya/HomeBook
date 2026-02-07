package com.fanda.homebook.stock.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.common.sheet.SheetTitleWidget
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 库存物品用完评论底部弹窗组件
 *
 * 功能：当用户标记库存物品用完时，收集使用反馈信息
 *
 * 收集的信息包括：
 * 1. 用完日期
 * 2. 用完后剩余量（可选）
 * 3. 使用感受（评分或标签）
 *
 * 使用场景：
 * - 用户点击"用完"按钮时弹出
 * - 用于收集物品使用数据，优化后续购买决策
 *
 * @param remain 当前选中的剩余量选项
 * @param date 当前选中的日期字符串（格式："2023-05-05"）
 * @param feel 当前选中的使用感受选项
 * @param modifier Compose修饰符，用于调整布局样式
 * @param visible 弹窗是否可见
 * @param onDismiss 弹窗关闭回调（用户点击外部或返回键）
 * @param onConfirm 确认按钮点击回调（用户完成信息填写后提交）
 * @param onDateClick 日期选择点击回调（通常会打开日期选择器）
 * @param onRemainClick 剩余量选项点击回调（参数：选中的剩余量文本）
 * @param onFeelClick 使用感受选项点击回调（参数：选中的感受文本）
 */
@Composable fun StockCommentBottomSheet(
    remain: String,
    date: String,
    feel: String,
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onDateClick: () -> Unit,
    onRemainClick: (String) -> Unit,
    onFeelClick: (String) -> Unit
) {
    // 统一的内边距配置，保持UI一致性
    val itemPadding = Modifier.padding(
        start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp
    )

    // 使用自定义的底部弹窗容器
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        // 垂直列布局，包含所有表单元素
        Column(
            modifier = modifier, verticalArrangement = Arrangement.Center
        ) {
            // 1. 弹窗标题区域（包含标题和确认按钮）
            SheetTitleWidget(title = "用完了") {
                onConfirm()
            }

            // 2. 日期选择区域（点击选择用完日期）
            ItemOptionMenu(
                title = "用完日期", showText = true, rightText = date, showDivider = true, modifier = itemPadding, onClick = { onDateClick() })

            // 3. 剩余量选择区域
            Text(
                style = TextStyle.Default, text = "用完后剩余量", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(
                    start = 24.dp, top = 24.dp, bottom = 20.dp
                )
            )

            // 剩余量选项水平排列（如：空瓶、较少、较多）
            Row(
                modifier = Modifier.padding(
                    start = 24.dp, bottom = 24.dp
                ), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 遍历预定义的剩余量选项数据
                LocalDataSource.remainData.forEach { remainOption ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp, fontSize = 14.sp, contentPadding = PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        ), modifier = Modifier.size(66.dp, 36.dp), text = remainOption, selected = remain == remainOption, // 当前选中的选项高亮
                        onClick = { onRemainClick(remainOption) })
                }
            }

            // 4. 分隔线
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp)
            )

            // 5. 使用感受选择区域
            Text(
                style = TextStyle.Default, text = "使用感受", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.padding(
                    start = 24.dp, top = 24.dp, bottom = 20.dp
                )
            )

            // 使用感受选项网格布局（通常选项较多，如：不好用、一般、好用等）
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 固定4列网格
                horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(
                    start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp
                )
            ) {
                // 遍历预定义的使用感受选项数据
                items(LocalDataSource.feelData, key = { it }) { feelOption ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp, fontSize = 14.sp, contentPadding = PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        ), text = feelOption, modifier = Modifier.size(66.dp, 36.dp), selected = feel == feelOption, // 当前选中的选项高亮
                        onClick = { onFeelClick(feelOption) })
                }
            }
        }
    }
}

// 注意 debug 环境下才能预览，release 不行
@Composable @Preview(showBackground = true) fun StockCommentBottomSheetPreview() {
    HomeBookTheme {
        StockCommentBottomSheet(visible = true, onDismiss = {}, remain = "1", date = "2023-05-05", feel = "1", onConfirm = {}, onDateClick = {}, onRemainClick = {}, onFeelClick = {})
    }
}

