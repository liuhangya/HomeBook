package com.fanda.homebook.book.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.book.entity.CategoryData
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.common.entity.TransactionAmountType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.tools.roundToString

/**
 * 月度分类金额项组件
 * 显示一个月中某个分类的汇总信息，包括分类图标、名称、交易笔数和总金额
 *
 * @param modifier Compose修饰符
 * @param item 分类数据对象，包含分类信息和交易数据
 * @param onItemClick 分类项点击回调，用于查看分类详情或交易列表
 */
@Composable fun MonthAmountItemWidget(
    modifier: Modifier = Modifier, item: CategoryData, onItemClick: (CategoryData) -> Unit
) {
    Box(modifier = modifier) {
        // 使用渐变圆角边框容器
        GradientRoundedBoxWithStroke(
            colors = listOf(
                Color.White.copy(alpha = 0.4f),  // 渐变起始颜色（半透明白）
                Color.White.copy(alpha = 0.2f)   // 渐变结束颜色（更透明白）
            ), modifier = Modifier
                .fillMaxWidth()          // 占满父容器宽度
                .height(64.dp)          // 固定高度64dp
        ) {
            // 内容行布局
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) }  // 点击整个分类项
                .padding(start = 15.dp)           // 左侧内边距15dp
                .fillMaxHeight(),                 // 占满父容器高度
                verticalAlignment = Alignment.CenterVertically  // 垂直居中对齐
            ) {
                // 分类图标容器
                Box(
                    contentAlignment = Alignment.Center,  // 内容居中
                    modifier = Modifier
                        .size(32.dp)                     // 固定尺寸32x32dp
                        .clip(CircleShape)                // 圆形裁剪
                        .background(Color.White)          // 白色背景
                ) {
                    // 分类图标
                    Image(
                        painter = painterResource(id = getCategoryIcon(item.subCategory?.type ?: 0)),  // 获取分类图标资源
                        contentDescription = null,       // 无障碍描述
                        modifier = Modifier.scale(0.8f)  // 图标缩放为80%大小
                    )
                }

                // 分类信息列
                Column(
                    modifier = Modifier.padding(start = 12.dp),  // 左侧间距12dp
                    verticalArrangement = Arrangement.Center     // 垂直居中
                ) {
                    // 分类名称
                    Text(
                        text = item.subCategory?.name ?: "",    // 子分类名称，为空时显示空字符串
                        fontWeight = FontWeight.Medium,         // 中等字重
                        fontSize = 14.sp,                       // 字号14sp
                        color = Color.Black                     // 黑色字体
                    )

                    // 交易笔数行
                    Row(
                        verticalAlignment = Alignment.CenterVertically,   // 垂直居中对齐
                        modifier = Modifier.padding(top = 0.dp)          // 顶部间距0dp
                    ) {
                        // 交易笔数文本
                        Text(
                            text = "${item.transactions.size}笔",        // 显示交易笔数
                            fontWeight = FontWeight.Medium,             // 中等字重
                            fontSize = 10.sp,                           // 小字号10sp
                            color = colorResource(id = R.color.color_84878C)  // 灰色字体
                        )
                    }
                }

                // 弹性空白，用于分隔左侧内容和右侧金额
                Spacer(modifier = Modifier.weight(1f))

                // 金额显示逻辑
                val amount = when (item.categoryType) {
                    TransactionAmountType.INCOME.ordinal -> {
                        // 收入：显示正号和金额
                        "+${item.totalAmount.toFloat().roundToString()}"
                    }

                    TransactionAmountType.EXPENSE.ordinal -> {
                        // 支出：显示负号和金额
                        "-${item.totalAmount.toFloat().roundToString()}"
                    }

                    else -> {
                        // 其他类型：直接显示金额
                        item.totalAmount.toFloat().roundToString()
                    }
                }

                // 金额颜色逻辑
                val color = when (item.categoryType) {
                    TransactionAmountType.INCOME.ordinal -> {
                        // 收入：蓝色
                        colorResource(id = R.color.color_106CF0)
                    }

                    TransactionAmountType.EXPENSE.ordinal -> {
                        // 支出：红色
                        colorResource(id = R.color.color_FF2822)
                    }

                    else -> {
                        // 其他类型：灰色
                        colorResource(id = R.color.color_84878C)
                    }
                }

                // 金额文本
                Text(
                    text = amount,                       // 格式化后的金额
                    fontSize = 18.sp,                    // 大字号18sp
                    modifier = Modifier.padding(end = 16.dp),  // 右侧间距16dp
                    color = color                        // 根据类型设置的颜色
                )
            }
        }
    }
}