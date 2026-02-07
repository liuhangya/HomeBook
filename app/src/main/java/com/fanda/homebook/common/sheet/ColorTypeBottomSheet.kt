package com.fanda.homebook.common.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.color.ColorTypeEntity

/**
 * 颜色类型选择底部弹窗组件
 * 以5列网格形式显示颜色选项，用于选择物品颜色
 *
 * @param colorList 颜色数据列表
 * @param color 当前选中的颜色实体（可为空）
 * @param visible 弹窗是否可见的函数
 * @param onDismiss 弹窗关闭回调函数
 * @param onConfirm 确认选择回调函数，返回选中的颜色实体
 * @param onSettingClick 设置按钮点击回调（用于跳转到颜色编辑页面，可选）
 */
@Composable fun ColorTypeBottomSheet(
    colorList: List<ColorTypeEntity>, color: ColorTypeEntity?, visible: () -> Boolean, onDismiss: () -> Unit, onConfirm: (ColorTypeEntity?) -> Unit, onSettingClick: (() -> Unit)? = null
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        // 记录当前选中的颜色
        var selected by remember { mutableStateOf(color) }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 弹窗标题栏
            SheetTitleWidget(
                title = "颜色", onSettingClick = onSettingClick
            ) {
                // 确认按钮点击逻辑
                onConfirm(selected)
                onDismiss()
            }

            // 颜色网格列表（5列布局）
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),  // 固定5列网格
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(
                    start = 24.dp, top = 0.dp, end = 24.dp, bottom = 24.dp
                )
            ) {
                items(colorList, key = { it.name }) { colorItem ->
                    Column(
                        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            // 注意顺序：先添加点击事件，后添加内边距
                            .clickable(
                                onClick = {
                                selected = colorItem  // 选中该颜色
                            }, interactionSource = remember { MutableInteractionSource() }, indication = null  // 去掉默认的点击效果
                            )
                            .padding(8.dp)  // 每个颜色项的间距
                    ) {
                        // 颜色圆形显示
                        ColoredCircleWithBorder(
                            color = Color(colorItem.color),  // 将整数值转换为Color
                            size = 24.dp,
                            borderColor = if (selected == colorItem) {
                                Color.Black  // 选中状态显示黑色边框
                            } else {
                                colorResource(R.color.color_EAF0F7)  // 未选中显示浅灰色边框
                            },
                        )

                        // 颜色名称和颜色圆圈的间距
                        Spacer(modifier = Modifier.height(8.dp))

                        // 颜色名称文本
                        Text(
                            text = colorItem.name, style = TextStyle.Default, fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}