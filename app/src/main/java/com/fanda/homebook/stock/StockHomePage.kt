package com.fanda.homebook.stock

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.ui.ClosetGridWidget
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.UserDropdownMenu
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.entity.BaseCategoryEntity
import com.fanda.homebook.entity.StateMenuEntity
import com.fanda.homebook.quick.sheet.SubCategory
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.stock.ui.StockGridWidget
import com.fanda.homebook.tools.LogUtils

/*
*
* 囤货页面
* */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun StockHomePage(modifier: Modifier = Modifier, navController: NavController) {
    var showSelectImage by remember { mutableStateOf(false) }
    var expandUserMenu by remember { mutableStateOf(false) }
    var curGoodRack by remember { mutableStateOf(LocalDataSource.goodsRackData.first()) }
    //  记录上次的返回时间
    var lastBackPressed by remember { mutableLongStateOf(0L) }

    var curStateMenu by remember { mutableStateOf(LocalDataSource.stockStateList.first()) }

    var subCategory by remember { mutableStateOf(SubCategory("", "全部")) }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopAppBar(
            title = {
                Box(
                    modifier = modifier
                        .height(64.dp)
                        .padding(start = 8.dp, end = 12.dp)
                        .fillMaxWidth()
                        .background(color = Color.Transparent)
                ) {

                    Box(modifier = Modifier
                        .wrapContentWidth()
                        .height(64.dp)      // 这里要固定高度，不然 pop 显示位置异常
                        .align(Alignment.CenterStart)
                        .clickable(
                            // 去掉默认的点击效果
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastBackPressed > 200 && !expandUserMenu) {
                                expandUserMenu = true
                            }
                            Log.d("ClosetHomePage", "点击了用户名")
                        }
                        .padding(start = 0.dp, end = 30.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(text = curGoodRack.name, fontWeight = FontWeight.Medium, fontSize = 18.sp, color = Color.Black)
                            Image(modifier = Modifier.padding(start = 6.dp), painter = painterResource(id = R.mipmap.icon_arrow_down_black), contentDescription = null)
                        }
                        UserDropdownMenu(curUser = curGoodRack, data = LocalDataSource.goodsRackData, expanded = expandUserMenu, dpOffset = DpOffset(0.dp, 50.dp), onDismiss = {
                            lastBackPressed = System.currentTimeMillis()
                            expandUserMenu = false
                        }, onConfirm = {
                            expandUserMenu = false
                            curGoodRack = it
                        })
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                showSelectImage = true
                            }) {
                            Image(
                                painter = painterResource(id = R.mipmap.icon_add_grady), contentDescription = "Action", contentScale = ContentScale.Fit, modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

            },
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent),
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            StateMenu(curMenuEntity = curStateMenu) {
                curStateMenu = it
            }
            val list = LocalDataSource.stockCategoryData.find { it.name == curGoodRack.name }?.children ?: emptyList()
            LabelMenu(list = list, subCategory = subCategory) {
                val selected = subCategory.selected
                subCategory = if (subCategory.id == it.id) {
                    it.copy(selected = !selected)
                } else {
                    it.copy(selected = true)
                }
                LogUtils.i("LabelMenu", "点击了标签 $subCategory")
            }

            StockGridWidget {

            }
        }

    }

    SelectPhotoBottomSheet(visible = showSelectImage, onDismiss = {
        showSelectImage = false
    }) {
        showSelectImage = false
        navController.navigate(RoutePath.AddStock.route)
    }

}

@Composable fun StateMenu(modifier: Modifier = Modifier, curMenuEntity: StateMenuEntity, onMenuChange: (StateMenuEntity) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        LocalDataSource.stockStateList.forEach {
            Text(text = "${it.name}(${it.count})",
                modifier = Modifier
                    .weight(1f)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onMenuChange(it)
                    },
                textAlign = TextAlign.Center,
                color = if (it.id == curMenuEntity.id) Color.Black else colorResource(id = R.color.color_83878C),
                fontWeight = if (it.id == curMenuEntity.id) FontWeight.Medium else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable fun LabelMenu(modifier: Modifier = Modifier, list: List<SubCategory>, subCategory: SubCategory, onLabelChange: (SubCategory) -> Unit) {
    LazyRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(20.dp)) {
        items(list) {
            LogUtils.d("item: ${it.id} , $subCategory - ${(subCategory.id == it.id && subCategory.selected)}")
            SelectableRoundedButton(text = it.name, selected = (subCategory.id == it.id && subCategory.selected), onClick = {
                onLabelChange(it)
            })
        }
    }
}


@Composable @Preview(showBackground = true) fun ClosetHomePagePreview() {
    StockHomePage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}