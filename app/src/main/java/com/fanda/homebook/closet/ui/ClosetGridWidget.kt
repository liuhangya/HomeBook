package com.fanda.homebook.closet.ui

import android.util.Log
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.ClosetGridEntity
import com.fanda.homebook.R

@Composable fun ClosetGridWidget(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(LocalDataSource.closetGridList) {
            ClosetGridItem(item = it)
        }
    }
}

@Composable fun ClosetGridItem(item: ClosetGridEntity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
        // 去掉默认的点击效果
        interactionSource = remember { MutableInteractionSource() }, indication = null
    ) {
        Log.d("ClosetGridItem", "点击了item: $item")
    }) {
        Box(
            modifier = Modifier
                .border(1.dp, Color.White, shape = RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                contentScale = ContentScale.Crop, model = R.mipmap.bg_closet_dufault, contentDescription = null, modifier = Modifier
                    .height(100.dp)
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Text(
                    text = item.count.toString(),
                    style = TextStyle.Default.copy(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier
                        .background(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    fontSize = 8.sp,
                    color = Color.Black
                )
            }
        }
        Text(text = item.name, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp, color = colorResource(id = R.color.color_333333))
    }
}


@Composable @Preview(showBackground = true) fun ClosetGridWidgetPreview() {
    ClosetGridWidget()
}