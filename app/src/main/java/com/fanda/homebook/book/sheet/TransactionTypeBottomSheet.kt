package com.fanda.homebook.book.sheet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
import com.fanda.homebook.common.sheet.SheetTitleWidget

/**
 * 交易类型选择底部弹窗
 * 用于选择交易类型（支出/收入分类）
 *
 * @param initial 初始选中的交易子分类
 * @param data 交易分类数据，包含主分类和子分类
 * @param title 弹窗标题
 * @param visible 是否显示弹窗
 * @param onDismiss 关闭弹窗回调
 * @param onSettingClick 设置按钮点击回调
 * @param onConfirm 确认选择回调，返回选中的交易子分类（null表示选择"全部类型"）
 */
@Composable fun TransactionTypeBottomSheet(
    initial: TransactionSubEntity?,
    data: List<TransactionWithSubCategories>,
    title: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSettingClick: () -> Unit,
    onConfirm: (TransactionSubEntity?) -> Unit
) {
    // 自定义底部弹窗组件
    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        // 选中的交易子分类状态
        var selected by remember { mutableStateOf(initial) }
        Column {
            // 弹窗标题栏，包含标题、设置按钮和确认按钮
            SheetTitleWidget(title = title, onSettingClick = onSettingClick) {
                onConfirm(selected)
                onDismiss()
            }

            // 可滚动的内容区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 内容内边距
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)) {
                    // "全部类型"选项
                    SelectableRoundedButton(
                        cornerSize = 8.dp,
                        fontSize = 14.sp,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                        modifier = Modifier.size(96.dp, 44.dp),
                        text = "全部类型",
                        selected = selected == null, // 当selected为null时表示选中"全部类型"
                        onClick = { selected = null })

                    // 遍历所有交易分类数据，显示每个主分类下的子分类
                    data.forEach { transactionWithCategories ->
                        TransactionTypeItem(
                            initial = selected, title = transactionWithCategories.category.name, data = transactionWithCategories.subCategories
                        ) { subEntity ->
                            selected = subEntity
                        }
                    }
                }
            }
        }
    }
}

/**
 * 交易类型项组件
 * 显示一个主分类及其所有子分类的按钮
 *
 * @param initial 当前选中的交易子分类
 * @param modifier 修饰符
 * @param title 主分类名称
 * @param data 子分类列表
 * @param onSelect 子分类选中回调
 */
@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun TransactionTypeItem(
    initial: TransactionSubEntity?, modifier: Modifier = Modifier, title: String, data: List<TransactionSubEntity?>, onSelect: (TransactionSubEntity?) -> Unit
) {
    Column(modifier = modifier) {
        // 主分类标题
        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp), text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
        )

        // 使用BoxWithConstraints获取父容器约束，动态计算子项宽度
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val itemSpacing = 16.dp      // 子项之间的间距
            val maxColumns = 3           // 最大列数
            val totalSpacing = itemSpacing * (maxColumns - 1)  // 总间距宽度
            // 计算每个子项的宽度：(父容器宽度 - 总间距) / 列数
            val itemWidth = (maxWidth - totalSpacing) / maxColumns

            // 流式布局，自动换行显示子分类按钮
            FlowRow(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing),     // 水平间距
                verticalArrangement = Arrangement.spacedBy(itemSpacing),       // 垂直间距
                maxItemsInEachRow = maxColumns                                // 每行最大项数
            ) {
                // 遍历所有子分类
                data.forEach { category ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp,                                    // 圆角大小
                        fontSize = 14.sp,                                     // 字体大小
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),  // 内边距
                        modifier = Modifier
                            .height(44.dp)                                    // 固定高度
                            .width(itemWidth),                                // 动态宽度
                        text = category?.name ?: "",                          // 子分类名称，空字符串处理null
                        // 判断当前子分类是否被选中：比较名称和ID
                        selected = initial?.name == category?.name && initial?.id == category?.id, onClick = { onSelect(category) }                      // 点击回调
                    )
                }
            }
        }
    }
}

/**
 * 预览函数
 */
@Composable @Preview(showBackground = true) fun TransactionTypeBottomSheetPreview() {
    TransactionTypeBottomSheet(
        initial = null, title = "选择类型", visible = true, onDismiss = {}, onConfirm = {}, onSettingClick = {}, data = emptyList()
    )
}