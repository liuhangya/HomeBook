package com.fanda.homebook.quick.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.fanda.homebook.data.LocalDataSource


@Composable fun SelectCategoryGrid() {
    GradientRoundedBoxWithStroke(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        var selectedCategory by remember { mutableIntStateOf(0) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(5), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {

            items(items = LocalDataSource.expenseCategoryData, key = { it.name }) { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    selectedCategory = category.icon
                }) {
                    // 通过 colorFilter 来改变图标颜色
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier
                            .size(32.dp)
                            .clip(
                                CircleShape
                            )
                            .background(if (selectedCategory == category.icon) Color.Black else Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = category.icon), contentDescription = null, colorFilter = if (selectedCategory == category.icon) ColorFilter.tint(
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