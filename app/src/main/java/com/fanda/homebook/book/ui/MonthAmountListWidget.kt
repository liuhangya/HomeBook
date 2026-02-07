package com.fanda.homebook.book.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.book.entity.CategoryData
import com.fanda.homebook.book.entity.MonthData
import com.fanda.homebook.data.quick.AddQuickEntity
import com.fanda.homebook.tools.roundToString

/**
 * 月度数据项组件
 * 显示一个月的汇总数据及其下的分类数据列表
 *
 * @param modifier Compose修饰符
 * @param item 月度数据对象，包含月份信息和分类数据
 * @param onMonthClick 月份标题点击回调，用于展开/收起或跳转到详细页面
 * @param onItemClick 分类数据项点击回调，用于查看分类详情
 */
@Composable fun MonthItemWidget(
    modifier: Modifier = Modifier, item: MonthData, onMonthClick: (MonthData) -> Unit, onItemClick: (CategoryData) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 月度汇总行：显示月份、支出和收入总额
        Row(
            modifier = Modifier
                .padding(top = 22.dp, bottom = 8.dp)
                .clickable(       // 去掉默认的点击效果，使用无涟漪效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    onMonthClick(item)  // 点击整个月份行时触发回调
                }, horizontalArrangement = Arrangement.SpaceBetween,  // 子项在水平方向均匀分布
            verticalAlignment = Alignment.CenterVertically      // 子项垂直居中对齐
        ) {
            // 月份显示文本
            Text(
                text = item.monthDisplay,      // 月份显示文本（如"2024年1月"）
                fontWeight = FontWeight.Medium, // 中等字重
                fontSize = 16.sp,               // 字号16sp
                color = Color.Black             // 黑色字体
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

        // 分类数据列表
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)  // 分类项之间间距8dp
        ) {
            // 遍历该月下的所有分类数据
            item.categories.forEach { categoryData ->
                MonthAmountItemWidget(
                    item = categoryData,        // 分类数据
                    onItemClick = onItemClick   // 分类项点击回调
                )
            }
        }
    }
}