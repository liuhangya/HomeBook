package com.fanda.homebook.book.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.data.quick.TransactionDateGroup
import com.fanda.homebook.tools.roundToString

/**
 * 每日交易分组项组件
 * 显示一天内的交易汇总信息及其明细列表
 *
 * @param modifier Compose修饰符
 * @param item 按日期分组的交易数据对象
 * @param onItemClick 交易项点击回调，用于查看或编辑单笔交易
 * @param onDelete 交易项删除回调，用于删除单笔交易
 */
@Composable fun DailyItemWidget(
    modifier: Modifier = Modifier, item: TransactionDateGroup, onItemClick: (AddQuickEntity) -> Unit, onDelete: (AddQuickEntity) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 每日汇总行：显示日期、支出和收入总额
        Row(
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween,  // 子项在水平方向均匀分布
            verticalAlignment = Alignment.CenterVertically      // 子项垂直居中对齐
        ) {
            // 日期格式文本（如"2024-01-15"）
            Text(
                text = item.dateFormat,       // 格式化后的日期字符串
                fontWeight = FontWeight.Medium,  // 中等字重
                fontSize = 16.sp,               // 字号16sp
                color = Color.Black             // 黑色字体
            )

            // 显示日期文本（如"星期一"）
            Text(
                modifier = Modifier.padding(start = 4.dp),  // 左侧间距4dp
                text = item.displayDate,                   // 显示的日期文本
                fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black
            )

            // 弹性空白，用于分隔左侧和右侧内容
            Spacer(modifier = Modifier.weight(1f))

            // 支出标签
            Text(
                text = "出",                     // 支出标签文字
                fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.Black, modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.4f),  // 半透明白色背景
                        shape = RoundedCornerShape(4.dp)        // 圆角矩形
                    )
                    .padding(horizontal = 4.dp, vertical = 0.dp) // 内边距
            )

            // 支出金额
            Text(
                modifier = Modifier.padding(start = 4.dp),  // 左侧间距4dp
                text = item.totalExpense.toFloat().roundToString(),  // 格式化后的支出金额
                fontWeight = FontWeight.Medium, fontSize = 16.sp, color = colorResource(id = R.color.color_FF2822)  // 支出金额颜色（红色）
            )

            // 收入标签
            Text(
                text = "入",                     // 收入标签文字
                fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.Black, modifier = Modifier
                    .padding(start = 12.dp)      // 左侧间距12dp
                    .background(
                        color = Color.White.copy(alpha = 0.4f),  // 半透明白色背景
                        shape = RoundedCornerShape(4.dp)        // 圆角矩形
                    )
                    .padding(horizontal = 4.dp, vertical = 0.dp) // 内边距
            )

            // 收入金额
            Text(
                modifier = Modifier.padding(start = 4.dp),  // 左侧间距4dp
                text = item.totalIncome.toFloat().roundToString(),  // 格式化后的收入金额
                fontWeight = FontWeight.Medium, fontSize = 16.sp, color = colorResource(id = R.color.color_106CF0)  // 收入金额颜色（蓝色）
            )
        }

        // 当日交易明细列表
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)  // 交易项之间间距8dp
        ) {
            // 遍历该日期下的所有交易记录
            item.transactions.forEach { transaction ->
                // 自定义长按工具提示组件，支持点击和删除操作
                DailyAmountItemWidget(
                    item = transaction,          // 单笔交易数据
                    onItemClick = onItemClick,   // 交易项点击回调
                    onDelete = onDelete,         // 交易项删除回调
                    enableClick = true           // 启用点击功能
                )
            }
        }
    }
}