package com.fanda.homebook.closet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.data.closet.ClosetSubCategoryGridItem

/**
 * 衣橱分类网格组件
 *
 * 用于在衣橱首页显示分类的网格布局
 *
 * @param data 分类数据列表
 * @param modifier 修饰符，用于自定义网格布局
 * @param onItemClick 网格项点击回调函数
 */
@Composable fun ClosetCategoryGridWidget(
    data: List<ClosetSubCategoryGridItem>, modifier: Modifier = Modifier, onItemClick: (ClosetSubCategoryGridItem) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), // 内容内边距
        columns = GridCells.Fixed(3),                                                // 固定3列网格
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp),                         // 水平间距
        verticalArrangement = Arrangement.spacedBy(20.dp)                            // 垂直间距
    ) {
        items(data) { item ->
            ClosetCategoryGridItemWidget(item = item, onItemClick)
        }
    }
}

/**
 * 衣橱分类网格项组件
 *
 * 显示单个分类项的图片、数量统计和名称
 *
 * @param item 分类项数据
 * @param onItemClick 点击回调函数
 */
@Composable fun ClosetCategoryGridItemWidget(
    item: ClosetSubCategoryGridItem, onItemClick: (ClosetSubCategoryGridItem) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中对齐
        modifier = Modifier.clickable(
            // 移除默认点击效果
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            onItemClick(item)
        }) {
        // 图片容器
        Box(
            modifier = Modifier
                .border(1.dp, Color.White, shape = RoundedCornerShape(12.dp))         // 白色边框
                .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)) // 半透明白色背景
        ) {
            // 异步加载图片
            AsyncImage(
                contentScale = ContentScale.Crop,                                      // 裁剪缩放
                model = item.imageLocalPath,                                           // 图片路径
                contentDescription = null,                                             // 无障碍描述
                modifier = Modifier
                    .height(100.dp)                                                   // 固定高度
                    .width(96.dp)                                                     // 固定宽度
                    .clip(RoundedCornerShape(12.dp))                                  // 圆角裁剪
            )

            // 数量统计标签
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)                                       // 右下角对齐
                    .padding(6.dp)                                                    // 内边距
            ) {
                Text(
                    text = item.count.toString(),                                     // 显示数量
                    style = TextStyle.Default.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false) // 移除字体内边距
                    ), modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),                  // 半透明白色背景
                            shape = RoundedCornerShape(8.dp)                         // 圆角形状
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp),                // 文本内边距
                    fontSize = 8.sp,                                                 // 字体大小
                    color = Color.Black                                              // 字体颜色
                )
            }
        }

        // 分类名称
        Text(
            text = item.category.name,                                                // 分类名称
            modifier = Modifier.padding(top = 8.dp),                                  // 顶部间距
            fontSize = 14.sp,                                                         // 字体大小
            color = colorResource(id = R.color.color_333333)                         // 深灰色字体
        )
    }
}