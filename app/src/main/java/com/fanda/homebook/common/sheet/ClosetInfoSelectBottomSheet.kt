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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.season.SeasonEntity
import kotlin.collections.minus
import kotlin.collections.plus

@Composable fun ClosetInfoSelectBottomSheet(
    seasonList: List<SeasonEntity>,
    colorList: List<ColorTypeEntity>,
    color: ColorTypeEntity?,
    season: SeasonEntity?,
    visible: () -> Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ColorTypeEntity?, SeasonEntity?) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        // 记录当前选中的颜色
        var selectedColor by remember { mutableStateOf(color) }
        var selectedSeason by remember { mutableStateOf(season) }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 弹窗标题栏
            SheetTitleWidget(title = "信息筛选") {
                // 确认按钮点击逻辑
                onConfirm(selectedColor, selectedSeason)
                onDismiss()
            }

            Text(
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp), text = "季节", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 24.dp, top = 4.dp, end = 24.dp, bottom = 18.dp
                ),
            ) {
                items(seasonList, key = { it.hashCode() }) { item ->
                    SelectableRoundedButton(
                        cornerSize = 8.dp, fontSize = 14.sp, contentPadding = PaddingValues(
                            horizontal = 0.dp, vertical = 0.dp
                        ), modifier = Modifier.size(DpSize(66.dp, 36.dp)), text = item.name, selected = selectedSeason == item, onClick = {
                            selectedSeason = if (selectedSeason == item) null else item
                        })
                }
            }

            Text(
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp), text = "颜色", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black
            )

            // 颜色网格列表（5列布局）
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),  // 固定5列网格
                modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(
                    start = 12.dp, top = 0.dp, end = 12.dp, bottom = 30.dp
                )
            ) {
                items(colorList, key = { it.name }) { colorItem ->
                    Column(
                        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                            // 注意顺序：先添加点击事件，后添加内边距
                            .clickable(
                                onClick = {
                                selectedColor = if (selectedColor == colorItem) null else colorItem
                            }, interactionSource = remember { MutableInteractionSource() }, indication = null  // 去掉默认的点击效果
                            )
                            .padding(8.dp)  // 每个颜色项的间距
                    ) {
                        // 颜色圆形显示
                        ColoredCircleWithBorder(
                            color = Color(colorItem.color),  // 将整数值转换为Color
                            size = 24.dp,
                            borderColor = if (selectedColor == colorItem) {
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