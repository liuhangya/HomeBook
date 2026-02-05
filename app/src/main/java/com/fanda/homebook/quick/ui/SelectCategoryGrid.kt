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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionType
import com.fanda.homebook.entity.TransactionCategory
import com.fanda.homebook.tools.LogUtils


@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun SelectCategoryGrid(
    modifier: Modifier = Modifier,  initial: TransactionSubEntity?, items: List<TransactionSubEntity>?, onItemClick: (TransactionSubEntity) -> Unit
) {
    GradientRoundedBoxWithStroke(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val itemSpacing = 35.dp
            val maxColumns = 5
            val totalSpacing = itemSpacing * (maxColumns - 1)
            val itemWidth = (maxWidth - totalSpacing - 42.dp) / maxColumns  // 根据父容器的宽度和间距，动态计算每个item的宽度

            FlowRow(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = maxColumns
            ) {
                items?.forEach { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.clickable(
                            // 去掉默认的点击效果
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) {
                            onItemClick(category)
                        }) {
                        // 通过 colorFilter 来改变图标颜色
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                                .size(itemWidth)
                                .clip(
                                    CircleShape
                                )
                                .background(if (initial?.name == category.name && initial.categoryId == category.categoryId) Color.Black else Color.White)
                        ) {
                            Image(
                                painter = painterResource(id = getCategoryIcon(category.type)), contentDescription = null, colorFilter = if (initial?.name == category.name && initial.categoryId == category.categoryId) ColorFilter.tint(
                                    Color.White
                                ) else null, modifier = Modifier.scale(0.8f)

                            )

                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name, fontSize = 8.sp, style = TextStyle.Default, color = colorResource(R.color.color_333333), modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}


fun getCategoryIcon(type: Int) = when (type) {
    TransactionType.DINING.type -> R.mipmap.icon_dining
    TransactionType.TRAFFIC.type -> R.mipmap.icon_traffic
    TransactionType.CLOTHING.type -> R.mipmap.icon_clothing
    TransactionType.SKINCARE.type -> R.mipmap.icon_skincare
    TransactionType.SHOPPING.type -> R.mipmap.icon_shopping
    TransactionType.SERVICES.type -> R.mipmap.icon_services
    TransactionType.HEALTH.type -> R.mipmap.icon_health
    TransactionType.PLAY.type -> R.mipmap.icon_play
    TransactionType.DAILY.type -> R.mipmap.icon_daily
    TransactionType.TRAVEL.type -> R.mipmap.icon_travel
    TransactionType.INSURANCE.type -> R.mipmap.icon_insurance
    TransactionType.RED_ENVELOPE.type -> R.mipmap.icon_red_envelope
    TransactionType.SOCIAL.type -> R.mipmap.icon_social
    TransactionType.SALARY.type -> R.mipmap.icon_salary
    TransactionType.GET_ENVELOPE.type -> R.mipmap.icon_get_money
    TransactionType.BONUS.type -> R.mipmap.icon_bonus
    TransactionType.FINANCE.type -> R.mipmap.icon_finance
    TransactionType.DEBTS.type -> R.mipmap.icon_debts
    TransactionType.OTHERS.type -> R.mipmap.icon_others
    TransactionType.ADD.type -> R.mipmap.icon_category_add
    else -> {
        R.mipmap.icon_category_custom
    }
}


//@Composable
//fun SelectCategoryGridItem(category: TransactionCategory, modifier: Modifier = Modifier) {
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = modifier.clickable(
//            // 去掉默认的点击效果
//            interactionSource = remember { MutableInteractionSource() },
//            indication = null
//        ) {
//            selectedCategory = category.icon
//        }) {
//        // 通过 colorFilter 来改变图标颜色
//        Box(
//            contentAlignment = Alignment.Center, modifier = Modifier
//                .size(32.dp)
//                .clip(
//                    CircleShape
//                )
//                .background(if (selectedCategory == category.icon) Color.Black else Color.White)
//        ) {
//            Image(
//                painter = painterResource(id = category.icon),
//                contentDescription = null,
//                colorFilter = if (selectedCategory == category.icon) ColorFilter.tint(
//                    Color.White
//                ) else null,
//                modifier = Modifier.scale(0.8f)
//
//            )
//
//        }
//        Spacer(modifier = Modifier.height(4.dp))
//        Text(
//            text = category.name,
//            fontSize = 8.sp,
//            style = TextStyle.Default,
//            color = colorResource(R.color.color_333333),
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        )
//    }
//}