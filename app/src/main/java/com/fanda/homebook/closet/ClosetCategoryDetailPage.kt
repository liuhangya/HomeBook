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
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.viewmodel.CategoryDetailClosetViewModel
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.closet.CategoryBottomMenuEntity
import com.fanda.homebook.data.closet.ClosetDetailGridItem
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.sheet.CategoryExpandBottomSheet
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/**
 * 衣橱分类详情页面
 *
 * 显示特定分类下的所有衣橱物品，支持多选编辑操作
 */
@Composable fun ClosetCategoryDetailPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: CategoryDetailClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 从ViewModel收集状态
    val uiState by closetViewModel.uiState.collectAsState()
    val closets by closetViewModel.closets.collectAsState()
    val selectedItems by closetViewModel.selectedItems.collectAsState()
    val categories by closetViewModel.categories.collectAsState()

    LogUtils.i("衣橱详细对象: $uiState")
    LogUtils.i("衣橱详细列表: $closets")

    // 处理返回键：编辑模式下退出编辑，非编辑模式下返回上一页
    BackHandler {
        if (uiState.isEditState) {
            closetViewModel.toggleEditState(false)
        } else {
            navController.navigateUp()
        }
    }

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = if (uiState.moveToTrash) "垃圾桶" else uiState.categoryName,
            onBackClick = {
                if (uiState.isEditState) {
                    closetViewModel.toggleEditState(false)
                } else {
                    navController.navigateUp()
                }
            },
            // 根据编辑状态显示不同的右侧按钮
            rightIconPainter = if (uiState.isEditState) null else painterResource(R.mipmap.icon_add_grady),
            rightNextIconPainter = if (uiState.isEditState) null else painterResource(R.mipmap.icon_edit_menu),
            rightText = if (uiState.isEditState) "取消" else "",
            onRightActionClick = { isTextButton ->
                if (isTextButton) {
                    // 取消编辑
                    closetViewModel.toggleEditState(false)
                    closetViewModel.clearAllSelection()
                } else {
                    // 添加衣橱物品
                    closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                }
            },
            onRightNextActionClick = {
                // 进入编辑模式
                closetViewModel.toggleEditState(true)
            })
    }, bottomBar = {
        // 编辑模式下的底部操作栏
        EditCategoryBottomBar(
            visible = uiState.isEditState, onItemClick = {
                when (it.type) {
                    ShowBottomSheetType.COPY -> {
                        // 复制操作
                        closetViewModel.updateSheetType(ShowBottomSheetType.COPY)
                    }

                    ShowBottomSheetType.DELETE -> {
                        // 删除操作
                        closetViewModel.updateSheetType(ShowBottomSheetType.DELETE)
                    }

                    ShowBottomSheetType.MOVE -> {
                        // 移动操作
                        closetViewModel.updateSheetType(ShowBottomSheetType.CATEGORY)
                    }

                    ShowBottomSheetType.ALL_SELECTED -> {
                        // 全选/取消全选
                        closetViewModel.updateAllSelection()
                    }

                    else -> {}
                }
            })
    }) { padding ->
        // 衣橱物品网格视图
        ClosetDetailGridWidget(
            data = closets, Modifier.padding(padding), onItemClick = { item ->
                if (uiState.isEditState) {
                    // 编辑模式下：切换选中状态
                    closetViewModel.toggleSelection(item.addClosetEntity.closet.id)
                } else {
                    // 非编辑模式：跳转到详情页面
                    navController.navigate("${RoutePath.WatchAndEditCloset.route}?closetId=${item.addClosetEntity.closet.id}")
                }
            }, isEditState = uiState.isEditState
        )
    }

    // 各种确认弹窗

    // 复制确认弹窗
    if (closetViewModel.showBottomSheet(ShowBottomSheetType.COPY)) {
        ConfirmDialog(title = "复制单品到当前分类？", onDismissRequest = {
            closetViewModel.dismissBottomSheet()
        }, onConfirm = {
            closetViewModel.dismissBottomSheet()
            closetViewModel.copyEntityDatabase()
        })
    }

    // 删除确认弹窗
    if (closetViewModel.showBottomSheet(ShowBottomSheetType.DELETE)) {
        ConfirmDialog(title = "是否确认删除？", onDismissRequest = {
            closetViewModel.dismissBottomSheet()
        }, onConfirm = {
            closetViewModel.dismissBottomSheet()
            closetViewModel.deleteEntityDatabase()
        })
    }

    // 图片选择弹窗
    SelectPhotoBottomSheet(
        visible = uiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) { selectedUri ->
        closetViewModel.dismissBottomSheet()
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${selectedUri}&categoryId=${uiState.categoryId}")
    }

    // 分类选择弹窗（用于移动操作）
    CategoryExpandBottomSheet(
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

/**
 * 衣橱物品网格组件
 *
 * @param data 衣橱物品数据列表
 * @param modifier 修饰符
 * @param onItemClick 物品点击回调
 * @param isEditState 是否处于编辑状态
 */
@Composable fun ClosetDetailGridWidget(
    data: List<ClosetDetailGridItem> = emptyList(), modifier: Modifier = Modifier, onItemClick: (ClosetDetailGridItem) -> Unit, isEditState: Boolean = false
) {
    LazyVerticalGrid(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp), columns = GridCells.Fixed(3), // 固定3列
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(data) { item ->
            ClosetDetailGridItem(
                item = item, onItemClick, isEditState
            )
        }
    }
}

/**
 * 单个衣橱物品网格项组件
 *
 * @param item 衣橱物品数据
 * @param onItemClick 点击回调
 * @param isEditState 是否处于编辑状态
 */
@Composable fun ClosetDetailGridItem(
    item: ClosetDetailGridItem, onItemClick: (ClosetDetailGridItem) -> Unit, isEditState: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) {
            onItemClick(item)
        }) {
        // 图片容器
        Box(
            modifier = Modifier
                .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
        ) {
            // 异步加载图片
            AsyncImage(
                contentScale = ContentScale.Crop,
                model = item.addClosetEntity.closet.imageLocalPath,
                contentDescription = null,
                modifier = Modifier
                    .height(100.dp)
                    .width(96.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // 编辑模式下的选中状态指示器
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                if (isEditState) {
                    Image(
                        painter = if (item.isSelected) painterResource(id = R.mipmap.icon_selected)
                        else painterResource(R.mipmap.icon_unselected), contentDescription = if (item.isSelected) "已选中" else "未选中", modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 编辑模式底部操作栏组件
 *
 * @param modifier 修饰符
 * @param visible 是否可见
 * @param onItemClick 菜单项点击回调
 */
@Composable private fun EditCategoryBottomBar(
    modifier: Modifier = Modifier, visible: Boolean, onItemClick: (CategoryBottomMenuEntity) -> Unit
) {
    // 动态高度动画
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
            // 遍历显示所有底部菜单项
            LocalDataSource.closetCategoryBottomMenuList.forEach { menuItem ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier
                    .clickable {
                        onItemClick(menuItem)
                    }
                    .weight(1f)
                    .fillMaxHeight()) {
                    Image(
                        painter = painterResource(menuItem.icon), contentDescription = menuItem.name, modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = menuItem.name, modifier = Modifier.padding(top = 4.dp), fontSize = 16.sp, color = colorResource(id = R.color.color_333333)
                    )
                }
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览衣橱分类详情页面
 */
@Composable @Preview(showBackground = true) fun ClosetCategoryDetailPagePreview() {
    ClosetCategoryDetailPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}