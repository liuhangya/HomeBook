package com.fanda.homebook.quick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.transaction.TransactionEntity

/**
 * 顶部类型选择器组件
 * 用于在快速记账页面显示分类选择器和日期选择器
 *
 * @param transactionType 当前选中的交易类型（主分类）
 * @param data 交易类型列表（主分类列表）
 * @param date 当前显示的日期字符串
 * @param modifier 修饰符
 * @param onDateClick 日期选择器点击回调
 * @param onTypeChange 交易类型变化回调
 */
@Composable fun TopTypeSelector(
    transactionType: TransactionEntity?, data: List<TransactionEntity>, date: String, modifier: Modifier = Modifier, onDateClick: () -> Unit = {}, onTypeChange: (TransactionEntity?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),  // 按钮之间间距8dp
        modifier = modifier
    ) {
        // 交易类型按钮（主分类）
        data.forEach { transactionEntity ->
            SelectableRoundedButton(
                text = transactionEntity.name, selected = transactionType?.name == transactionEntity.name,  // 判断是否选中
                onClick = { onTypeChange(transactionEntity) }  // 点击时切换选中状态
            )
        }

        // 弹性空白，将日期选择按钮推到最右侧
        Spacer(modifier = Modifier.weight(1f))

        // 日期选择按钮
        SelectableRoundedButton(
            text = date, selected = false,            // 日期按钮通常不显示选中状态
            onClick = onDateClick,       // 点击触发日期选择
            imageRes = R.mipmap.icon_down, // 右侧下拉箭头图标
            interaction = true           // 启用交互效果
        )
    }
}