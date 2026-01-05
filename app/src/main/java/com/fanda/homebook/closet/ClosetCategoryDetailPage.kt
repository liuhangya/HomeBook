package com.fanda.homebook.closet

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.CategoryBottomMenuEntity
import com.fanda.homebook.entity.ClosetCategoryBottomMenuType
import com.fanda.homebook.entity.ClosetGridEntity

/*
*
* 衣橱详情页面
* */
@Composable fun ClosetCategoryDetailPage(modifier: Modifier = Modifier, navController: NavController) {
    var isEditState by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(ClosetGridEntity("", 0, "")) }

    BackHandler {
        if (isEditState) {
            isEditState = false
        } else {
            navController.navigateUp()
        }
    }
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(title = "上装",
            onBackClick = {
                if (isEditState) {
                    isEditState = false
                } else {
                    navController.navigateUp()
                }
            },
            rightIconPainter = if (isEditState) null else painterResource(R.mipmap.icon_add_grady),
            rightNextIconPainter = if (isEditState) null else painterResource(R.mipmap.icon_edit_menu),
            rightText = if (isEditState) "取消" else "",
            onRightActionClick = {
                isEditState = false
            },
            onRightNextActionClick = {
                isEditState = true
            })
    }, bottomBar = {
        EditCategoryBottomBar(visible = isEditState, onItemClick = {
            when (it.type) {
                ClosetCategoryBottomMenuType.COPY -> {
                    showCopyDialog = true
                }

                ClosetCategoryBottomMenuType.DELETE -> {
                    showDeleteDialog = true
                }

                ClosetCategoryBottomMenuType.MOVE -> {
                    Log.d("EditClosetCategoryPage", "点击了移动")
                }

                ClosetCategoryBottomMenuType.ALL_SELECTED -> {}
            }
        })
    }) { padding ->
        ClosetDetailGridWidget(Modifier.padding(padding), onItemClick = {
            category = it
        }, isEditState = isEditState)
    }

    if (showCopyDialog) {
        ConfirmDialog(title = "复制单品到当前分类？", onDismissRequest = {
            showCopyDialog = false
        }, onConfirm = {
            showCopyDialog = false
            Log.d("EditClosetCategoryPage", "点击了确定")
        })
    }
    if (showDeleteDialog) {
        ConfirmDialog(title = "是否确认删除？", onDismissRequest = {
            showDeleteDialog = false
        }, onConfirm = {
            showDeleteDialog = false
            Log.d("EditClosetCategoryPage", "点击了确定")
        })
    }

}

@Composable fun ClosetDetailGridWidget(modifier: Modifier = Modifier, onItemClick: (ClosetGridEntity) -> Unit, isEditState: Boolean = false) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(LocalDataSource.closetDetailGridList) {
            ClosetDetailGridItem(item = it, onItemClick, isEditState)
        }
    }
}


@Composable fun ClosetDetailGridItem(item: ClosetGridEntity, onItemClick: (ClosetGridEntity) -> Unit, isEditState: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
        // 去掉默认的点击效果
        interactionSource = remember { MutableInteractionSource() }, indication = null
    ) {
        onItemClick(item)
    }) {
        Box(
            modifier = Modifier
                .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                contentScale = ContentScale.Crop, model = R.mipmap.bg_closet_dufault, contentDescription = null, modifier = Modifier
                    .height(100.dp)
                    .width(96.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                if (isEditState) {
                    Image(
                        painter = if (item.isSelected) painterResource(id = R.mipmap.icon_selected) else painterResource(R.mipmap.icon_unselected),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable private fun EditCategoryBottomBar(modifier: Modifier = Modifier, visible: Boolean, onItemClick: (CategoryBottomMenuEntity) -> Unit) {
    // 动态高度
    val animatedHeight: Dp by animateDpAsState(
        if (visible) 72.dp else 0.dp, label = "底部划入划出动画"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .background(color = colorResource(id = R.color.color_E3EBF5))
            .border(1.dp, Color.White)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(modifier = Modifier.padding(horizontal = 0.dp)) {
            LocalDataSource.closetCategoryBottomMenuList.forEach {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier
                    .clickable {
                        onItemClick(it)
                    }
                    .weight(1f)
                    .fillMaxHeight()) {
                    Image(
                        painter = painterResource(it.icon), contentDescription = null, modifier = Modifier.size(24.dp)
                    )
                    Text(text = it.name, modifier = Modifier.padding(top = 4.dp), fontSize = 16.sp, color = colorResource(id = R.color.color_333333))
                }
            }
        }
    }
}


@Composable @Preview(showBackground = true) fun ClosetCategoryDetailPagePreview() {
    ClosetCategoryDetailPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}