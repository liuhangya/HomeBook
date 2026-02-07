package com.fanda.homebook.closet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.common.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.SubCategoryViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.category.SubCategoryEntity
import com.fanda.homebook.tools.LogUtils

/**
 * 编辑子分类页面
 *
 * 用于管理子分类（二级分类）的添加、编辑、删除和排序
 */
@Composable fun EditSubCategoryPage(
    modifier: Modifier = Modifier, navController: NavController, subCategoryViewModel: SubCategoryViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 从ViewModel收集状态
    val categories by subCategoryViewModel.categories.collectAsState()
    val uiState by subCategoryViewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = uiState.categoryName, // 显示父分类名称
                onBackClick = {
                    navController.navigateUp()
                },
                rightIconPainter = painterResource(R.mipmap.icon_add_grady), // 添加按钮
                onRightActionClick = {
                    subCategoryViewModel.toggleAddDialog(true) // 显示添加对话框
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        // 子分类拖拽排序列表
        SubCategoryDragWidget(
            Modifier.padding(padding), data = categories, onEditClick = {
                // 编辑分类点击
                subCategoryViewModel.updateEntity(it)
                subCategoryViewModel.toggleRenameOrDeleteBottomSheet(true)
            }) { from, to, items ->
            // 拖拽排序回调
            LogUtils.d("拖动 from: $from, to: $to ")
            subCategoryViewModel.updateSortOrders(items)
        }
    }

    // 重命名对话框
    if (uiState.editDialog) {
        EditDialog(title = "重命名", value = uiState.entity?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            subCategoryViewModel.toggleEditDialog(false)
        }, onConfirm = {
            subCategoryViewModel.toggleEditDialog(false)
            subCategoryViewModel.updateEntityDatabase(it)
        })
    }

    // 添加对话框
    if (uiState.addDialog) {
        EditDialog(title = "添加", value = "", placeholder = "不能与已有名称重复", onDismissRequest = {
            subCategoryViewModel.toggleAddDialog(false)
        }, onConfirm = {
            subCategoryViewModel.insertWithAutoOrder(it)
        })
    }

    // 重命名/删除底部弹窗
    RenameOrDeleteBottomSheet(
        visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
            subCategoryViewModel.toggleRenameOrDeleteBottomSheet(false)
        }) {
        subCategoryViewModel.toggleRenameOrDeleteBottomSheet(false)
        when (it) {
            RenameOrDeleteType.RENAME -> {
                // 重命名操作
                subCategoryViewModel.toggleEditDialog(true)
            }

            RenameOrDeleteType.DELETE -> {
                // 删除操作
                subCategoryViewModel.deleteEntityDatabase()
            }
        }
    }
}

/**
 * 子分类拖拽排序列表组件
 *
 * @param modifier 修饰符
 * @param data 子分类数据列表
 * @param onEditClick 编辑按钮点击回调
 * @param onMove 拖拽排序回调
 */
@Composable private fun SubCategoryDragWidget(
    modifier: Modifier = Modifier, data: List<SubCategoryEntity>, onEditClick: (SubCategoryEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<SubCategoryEntity>) -> Unit
) {
    // 调试日志：打印所有分类
    data.forEach {
        LogUtils.d("item: $it")
    }

    DragLazyColumn(
        modifier = modifier, items = data.map { it.copy() }.toMutableList(), // 创建可变副本
        onMove = onMove, key = { it.id } // 使用ID作为唯一键
    ) { item, isDragging ->
        // 单个子分类项
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent
                    )
                    .height(64.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                // 分类名称
                Text(
                    text = item.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                // 编辑按钮
                Image(
                    painter = painterResource(id = R.mipmap.icon_edit), contentDescription = "编辑", modifier = Modifier
                        .padding(7.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) {
                            onEditClick(item)
                        })

                // 拖拽手柄
                Image(
                    painter = painterResource(id = R.mipmap.icon_drag), contentDescription = "拖拽排序", modifier = Modifier.padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                )
            }
        }
    }
}