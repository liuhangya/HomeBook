package com.fanda.homebook.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R

/**
 * 衣柜分类选择组件
 *
 * 用于显示两级分类信息的选择项，支持一级分类和二级分类的显示
 *
 * @param firstType 一级分类名称
 * @param secondType 二级分类名称
 * @param modifier 修饰符，用于自定义整体布局
 * @param onClick 点击回调函数（可选）
 * @param dividerPadding 分割线的水平内边距，默认为20.dp
 */
@Composable fun SelectClosetCategoryWidget(
    firstType: String,
    secondType: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    dividerPadding: Dp = 20.dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick?.invoke() // 点击事件回调
            }) {
        // 内容区域
        Column(
            verticalArrangement = Arrangement.Center, modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically // 垂直居中对齐
            ) {
                // 左侧标题
                Text(
                    style = TextStyle.Default, text = "分类", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 16.sp
                )

                // 占位Spacer，将右侧内容推到最右
                Spacer(modifier = Modifier.weight(1f))

                // 根据分类数据的显示逻辑
                if (firstType.isEmpty() && secondType.isEmpty()) {
                    // 没有选择任何分类时：只显示右侧箭头
                    Image(
                        painter = painterResource(R.mipmap.icon_right), contentDescription = null
                    )
                } else {
                    // 有分类数据时的显示逻辑
                    if (firstType.isNotEmpty() && secondType.isNotEmpty()) {
                        // 同时有一级和二级分类：显示"一级分类 > 二级分类 > 箭头"
                        Text(
                            style = TextStyle.Default, text = firstType, color = colorResource(R.color.color_333333), // 深灰色文本
                            fontSize = 16.sp
                        )

                        // 分类之间的箭头分隔符（灰色）
                        Image(
                            painter = painterResource(R.mipmap.icon_right), contentDescription = null, modifier = Modifier.padding(horizontal = 7.dp), // 左右间距
                            colorFilter = ColorFilter.tint(colorResource(R.color.color_CFD5DE)) // 灰色调
                        )

                        // 二级分类文本
                        Text(
                            style = TextStyle.Default, text = secondType, color = colorResource(R.color.color_333333), // 深灰色文本
                            fontSize = 16.sp
                        )

                        // 右侧箭头图标
                        Image(
                            painter = painterResource(R.mipmap.icon_right), contentDescription = null, modifier = Modifier.padding(start = 9.dp) // 左侧间距
                        )
                    } else if (firstType.isNotEmpty() && secondType.isEmpty()) {
                        // 只有一级分类：显示"一级分类 > 箭头"
                        Text(
                            style = TextStyle.Default, text = firstType, color = colorResource(R.color.color_333333), // 深灰色文本
                            fontSize = 16.sp
                        )

                        // 右侧箭头图标
                        Image(
                            painter = painterResource(R.mipmap.icon_right), contentDescription = null, modifier = Modifier.padding(start = 9.dp) // 左侧间距
                        )
                    }
                    // 注：没有处理只有二级分类没有一级分类的情况，因为逻辑上二级分类必须有一级分类
                }
            }
        }

        // 底部分割线
        HorizontalDivider(
            thickness = 0.5.dp, color = colorResource(R.color.color_D9E1EB), // 浅蓝色分割线
            modifier = Modifier.padding(horizontal = dividerPadding)
        )
    }
}