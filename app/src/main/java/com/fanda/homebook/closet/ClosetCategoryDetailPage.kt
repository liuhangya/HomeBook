package com.fanda.homebook.closet

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.viewmodel.CategoryDetailClosetViewModel
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.closet.CategoryBottomMenuEntity
import com.fanda.homebook.data.closet.ClosetDetailGridItem
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.quick.sheet.CategoryBottomSheet
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/*
*
* 衣橱详情页面
* */
@Composable fun ClosetCategoryDetailPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: CategoryDetailClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    val uiState by closetViewModel.uiState.collectAsState()
    val closets by closetViewModel.closets.collectAsState()
    val selectedItems by closetViewModel.selectedItems.collectAsState()
    val categories by closetViewModel.categories.collectAsState()

    LogUtils.i("衣橱详细对象: $uiState")
    LogUtils.i("衣橱详细列表: $closets")

    BackHandler {
        if (uiState.isEditState) {
            closetViewModel.toggleEditState(false)
        } else {
            navController.navigateUp()
        }
    }
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title =  if (uiState.moveToTrash) "垃圾桶" else uiState.categoryName,
            onBackClick = {
                if (uiState.isEditState) {
                    closetViewModel.toggleEditState(false)
                } else {
                    navController.navigateUp()
                }
            },
            rightIconPainter = if (uiState.isEditState) null else painterResource(R.mipmap.icon_add_grady),
            rightNextIconPainter = if (uiState.isEditState) null else painterResource(R.mipmap.icon_edit_menu),
            rightText = if (uiState.isEditState) "取消" else "",
            onRightActionClick = { isTextButton ->
                if (isTextButton) {
                    closetViewModel.toggleEditState(false)
                    closetViewModel.clearAllSelection()
                } else {
                    closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                }
            },
            onRightNextActionClick = {
                closetViewModel.toggleEditState(true)
            })
    }, bottomBar = {
        EditCategoryBottomBar(visible = uiState.isEditState, onItemClick = {
            when (it.type) {
                ShowBottomSheetType.COPY -> {
                    closetViewModel.updateSheetType(ShowBottomSheetType.COPY)
                }

                ShowBottomSheetType.DELETE -> {
                    closetViewModel.updateSheetType(ShowBottomSheetType.DELETE)
                }

                ShowBottomSheetType.MOVE -> {
                    closetViewModel.updateSheetType(ShowBottomSheetType.CATEGORY)
                }

                ShowBottomSheetType.ALL_SELECTED -> {
                    closetViewModel.updateAllSelection()
                }

                else -> {}
            }
        })
    }) { padding ->
        ClosetDetailGridWidget(data = closets, Modifier.padding(padding), onItemClick = {
            if (uiState.isEditState) {
                closetViewModel.toggleSelection(it.addClosetEntity.closet.id)
            } else {
                // 跳转到详细页面 
                navController.navigate("${RoutePath.WatchAndEditCloset.route}?closetId=${it.addClosetEntity.closet.id}")
            }

        }, isEditState = uiState.isEditState)
    }

    if (closetViewModel.showBottomSheet(ShowBottomSheetType.COPY)) {
        ConfirmDialog(title = "复制单品到当前分类？", onDismissRequest = {
            closetViewModel.dismissBottomSheet()
        }, onConfirm = {
            closetViewModel.dismissBottomSheet()
            closetViewModel.copyEntityDatabase()
        })
    }
    if (closetViewModel.showBottomSheet(ShowBottomSheetType.DELETE)) {
        ConfirmDialog(title = "是否确认删除？", onDismissRequest = {
            closetViewModel.dismissBottomSheet()
        }, onConfirm = {
            closetViewModel.dismissBottomSheet()
            closetViewModel.deleteEntityDatabase()
        })
    }

    SelectPhotoBottomSheet(
        visible = uiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) {
        closetViewModel.dismissBottomSheet()
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${it}")
    }

    CategoryBottomSheet(
        categories = categories,
        categoryEntity = selectedItems.firstOrNull()?.category,
        subCategoryEntity = selectedItems.firstOrNull()?.subCategory,
        visible = { uiState.sheetType == ShowBottomSheetType.CATEGORY },
        onDismiss = {
            closetViewModel.dismissBottomSheet()
        },
        onConfirm = { category, subCategory ->
            LogUtils.i("选中的分类： $category, $subCategory")
            closetViewModel.updateEntityDatabase(category, subCategory)
        })

}

@Composable fun ClosetDetailGridWidget(data: List<ClosetDetailGridItem> = emptyList(), modifier: Modifier = Modifier, onItemClick: (ClosetDetailGridItem) -> Unit, isEditState: Boolean = false) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(data) {
            ClosetDetailGridItem(item = it, onItemClick, isEditState)
        }
    }
}


@Composable fun ClosetDetailGridItem(item: ClosetDetailGridItem, onItemClick: (ClosetDetailGridItem) -> Unit, isEditState: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
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
                contentScale = ContentScale.Crop,
                model = item.addClosetEntity.closet.imageLocalPath,
                contentDescription = null,
                modifier = Modifier
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