package com.fanda.homebook.stock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.data.stock.AddStockEntity
import com.fanda.homebook.data.stock.StockUseStatus
import com.fanda.homebook.data.stock.getStockDes
import com.fanda.homebook.data.stock.visibleExpireTime

/**
 * 库存物品网格布局组件
 * 以3列网格形式展示库存物品列表
 *
 * @param modifier Compose修饰符，用于调整布局样式
 * @param data 要显示的库存物品数据列表
 * @param onItemClick 网格项点击回调函数，参数为被点击的物品实体
 */
@Composable fun StockGridWidget(
    modifier: Modifier = Modifier, data: List<AddStockEntity>, onItemClick: (AddStockEntity) -> Unit
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, bottom = 20.dp
        ), columns = GridCells.Fixed(3),  // 固定3列网格布局
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp),  // 水平间距16dp
        verticalArrangement = Arrangement.spacedBy(16.dp)     // 垂直间距16dp
    ) {
        items(data) { item ->
            StockGridItem(item = item, onItemClick = onItemClick)
        }
    }
}

/**
 * 单个库存物品网格项组件
 * 显示物品图片、标签和基本信息
 *
 * @param item 要显示的库存物品实体数据
 * @param onItemClick 点击回调函数，参数为被点击的物品实体
 */
@Composable fun StockGridItem(
    item: AddStockEntity, onItemClick: (AddStockEntity) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
            // 去掉默认的点击效果
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            onItemClick(item)
        }) {
        // 图片容器，包含边框和背景
        Box(
            modifier = Modifier
                .border(1.dp, Color.White, shape = RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
        ) {
            // 物品图片
            AsyncImage(
                contentScale = ContentScale.Crop, model = item.stock.imageLocalPath, contentDescription = null, modifier = Modifier
                    .height(100.dp)  // 固定高度100dp
                    .clip(RoundedCornerShape(12.dp))
            )

            // 标签1：评论或使用感受标签（右上角）
            if (item.stock.comment.isNotEmpty() || item.stock.feel.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = if (item.stock.useStatus == StockUseStatus.USED.code) {
                            // 已用完状态显示使用感受
                            item.stock.feel
                        } else {
                            // 其他状态显示评论
                            item.stock.comment
                        }, style = TextStyle.Default.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ), modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.sp, color = Color.White
                    )
                }
            }

            // 标签2：子分类标签（左上角）
            if (item.subCategoryEntity != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = item.subCategoryEntity.name, style = TextStyle.Default.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ), modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.sp, color = Color.White
                    )
                }
            }

            // 标签3：过期时间标签（底部居中）
            if (item.stock.visibleExpireTime()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 12.dp, bottomEnd = 12.dp
                            )
                        )
                ) {
                    Text(
                        text = item.stock.getStockDes(), textAlign = TextAlign.Center, style = TextStyle.Default.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ), modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 5.dp), fontSize = 8.sp, color = Color.White
                    )
                }
            }
        }

        // 物品名称文本（图片下方）
        Text(
            text = item.stock.name, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp, color = colorResource(id = R.color.color_333333)
        )
    }
}