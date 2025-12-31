package com.fanda.homebook.closet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.UserDropdownMenu
import com.fanda.homebook.components.CustomTopAppBar
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.ClosetGridEntity
import com.fanda.homebook.route.RoutePath

/*
*
* 衣橱详情页面
* */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun ClosetCategoryDetailPage(modifier: Modifier = Modifier, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(ClosetGridEntity("", 0, "")) }
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopAppBar(navigationIcon =  { Image(painter = painterResource(id = R.mipmap.icon_back) , contentDescription = null)}, title = {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .height(56.dp)

                    .zIndex(1f)
            ) {
                Text(text = "上装", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black)
                Image(modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null)
            }
        }, colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent), actions = {
            Image(
                modifier = Modifier.clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                    Log.d("ClosetHomePage", "点击了添加按钮")
                }, painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = null
            )
            Spacer(modifier = Modifier.width(20.dp))
            Image(
                modifier = Modifier.clickable(
                    // 去掉默认的点击效果
                    interactionSource = remember { MutableInteractionSource() }, indication = null
                ) {
                }, painter = painterResource(id = R.mipmap.icon_edit_menu), contentDescription = null
            )
        }, modifier = Modifier.padding(end = 20.dp))
    }) { padding ->
//        CategoryListWidget(Modifier.padding(padding), onItemClick = {
//            category = it
//            showDialog = true
//        })

        ClosetDragWidget(Modifier.padding(padding), onItemClick = {
            category = it
            showDialog = true
        })


    }

    if (showDialog) {
        EditDialog(title = "添加分类", value = category.name, placeholder = "不能与已有类型名重复", onDismissRequest = {
            showDialog = false
        }, onConfirm = {
            showDialog = false
            Log.d("EditClosetCategoryPage", "点击了确定： $it")
        })
    }

}

@Composable private fun ClosetDragWidget(modifier: Modifier = Modifier, onItemClick: (ClosetGridEntity) -> Unit) {
    DragLazyColumn(modifier = modifier, items = LocalDataSource.closetGridList, onMove = { from, to ->
        Log.d("EditClosetCategoryPage", "from: $from, to: $to")
    }) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black)
                Image(
                    painter = painterResource(id = R.mipmap.icon_right), contentDescription = null, modifier = Modifier.size(4.dp, 8.dp), colorFilter = ColorFilter.tint(Color.Black)
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(painter = painterResource(id = R.mipmap.icon_edit),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(7.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onItemClick(item)
                        })

                Image(
                    painter = painterResource(id = R.mipmap.icon_drag), contentDescription = null, modifier = Modifier.padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                )

            }
        }


    }
}

@Composable private fun CategoryListWidget(modifier: Modifier = Modifier, onItemClick: (ClosetGridEntity) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier, contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, bottom = 20.dp
        )
    ) {
        items(LocalDataSource.closetGridList) {
            GradientRoundedBoxWithStroke {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = it.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black)
                    Image(
                        painter = painterResource(id = R.mipmap.icon_right), contentDescription = null, modifier = Modifier.size(4.dp, 8.dp), colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(painter = painterResource(id = R.mipmap.icon_edit),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(7.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onItemClick(it)
                            })

                    Image(painter = painterResource(id = R.mipmap.icon_drag),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                Log.d("EditClosetCategoryPage", "点击了拖动： $it")
                            })
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