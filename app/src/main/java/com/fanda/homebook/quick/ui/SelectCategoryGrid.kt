package com.fanda.homebook.quick.ui

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionType

/**
 * 分类选择网格组件
 * 以网格形式显示交易分类，支持动态布局和多列显示
 *
 * @param modifier 修饰符
 * @param initial 初始选中的分类，用于高亮显示
 * @param items 分类列表
 * @param onItemClick 分类项点击回调
 */
@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun SelectCategoryGrid(
    modifier: Modifier = Modifier, initial: TransactionSubEntity?, items: List<TransactionSubEntity>?, onItemClick: (TransactionSubEntity) -> Unit
) {
    // 渐变圆角边框容器，支持内容大小动画
    GradientRoundedBoxWithStroke(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()  // 内容变化时添加动画
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 布局参数
            val itemSpacing = 35.dp          // 分类项之间的间距
            val maxColumns = 5               // 最大列数
            val totalSpacing = itemSpacing * (maxColumns - 1)  // 列间距总和
            val itemInternalPadding = 42.dp  // 内部padding（经验值）

            // 计算每个分类项的宽度：可用宽度 / 列数
            val itemWidth = (maxWidth - totalSpacing - itemInternalPadding) / maxColumns

            // 流式布局（自动换行）
            FlowRow(
                modifier = Modifier
                    .padding(20.dp)  // 容器内边距
                    .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing),  // 水平间距
                verticalArrangement = Arrangement.spacedBy(14.dp),         // 垂直间距
                maxItemsInEachRow = maxColumns                             // 每行最大项数
            ) {
                // 遍历显示所有分类项
                items?.forEach { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null  // 移除默认点击效果
                        ) {
                            onItemClick(category)
                        }) {
                        // 分类图标容器
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .size(itemWidth)  // 动态宽度
                                .clip(CircleShape)  // 圆形
                                .background(
                                    if (initial?.name == category.name && initial.categoryId == category.categoryId) Color.Black  // 选中状态：黑色背景
                                    else Color.White  // 未选中状态：白色背景
                                )
                        ) {
                            Image(
                                painter = painterResource(id = getCategoryIcon(category.type)),
                                contentDescription = null,
                                colorFilter = if (initial?.name == category.name && initial.categoryId == category.categoryId) ColorFilter.tint(Color.White)  // 选中状态：白色图标
                                else null,  // 未选中状态：原始图标颜色
                                modifier = Modifier.scale(0.8f)  // 缩放80%
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))  // 图标和文字之间的间距

                        // 分类名称
                        Text(
                            text = category.name, fontSize = 8.sp,                // 小字号
                            style = TextStyle.Default, color = colorResource(R.color.color_333333),  // 深灰色文字
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 根据分类类型获取对应的图标资源
 *
 * @param type 分类类型值
 * @return 对应的图标资源ID
 */
fun getCategoryIcon(type: Int) = when (type) {
    TransactionType.DINING.type -> R.mipmap.icon_dining          // 餐饮
    TransactionType.TRAFFIC.type -> R.mipmap.icon_traffic       // 交通
    TransactionType.CLOTHING.type -> R.mipmap.icon_clothing     // 服装
    TransactionType.SKINCARE.type -> R.mipmap.icon_skincare     // 护肤
    TransactionType.SHOPPING.type -> R.mipmap.icon_shopping     // 购物
    TransactionType.SERVICES.type -> R.mipmap.icon_services     // 服务
    TransactionType.HEALTH.type -> R.mipmap.icon_health         // 健康
    TransactionType.PLAY.type -> R.mipmap.icon_play             // 娱乐
    TransactionType.DAILY.type -> R.mipmap.icon_daily           // 日常
    TransactionType.TRAVEL.type -> R.mipmap.icon_travel         // 旅行
    TransactionType.INSURANCE.type -> R.mipmap.icon_insurance   // 保险
    TransactionType.RED_ENVELOPE.type -> R.mipmap.icon_red_envelope  // 红包
    TransactionType.SOCIAL.type -> R.mipmap.icon_social         // 社交
    TransactionType.SALARY.type -> R.mipmap.icon_salary         // 工资
    TransactionType.GET_ENVELOPE.type -> R.mipmap.icon_get_money // 收红包
    TransactionType.BONUS.type -> R.mipmap.icon_bonus           // 奖金
    TransactionType.FINANCE.type -> R.mipmap.icon_finance       // 理财
    TransactionType.DEBTS.type -> R.mipmap.icon_debts           // 债务
    TransactionType.OTHERS.type -> R.mipmap.icon_others         // 其他
    TransactionType.ADD.type -> R.mipmap.icon_category_add      // 添加
    else -> {
        R.mipmap.icon_category_custom  // 默认自定义图标
    }
}
