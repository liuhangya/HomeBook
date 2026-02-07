package com.fanda.homebook.common

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.common.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.SizeViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.size.defaultSizeData
import com.fanda.homebook.tools.LogUtils

/**
 * 编辑尺码页面
 * 用于管理尺码的CRUD操作和排序
 * @param modifier 修饰符
 * @param navController 导航控制器，用于页面跳转
 * @param sizeViewModel 尺码ViewModel，由Compose自动注入
 */
@Composable fun EditSizePage(
    modifier: Modifier = Modifier, navController: NavController, sizeViewModel: SizeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    // 通过ViewModel状态管理进行数据绑定
    val sizes by sizeViewModel.sizes.collectAsState()

    val uiState by sizeViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "尺码管理",
                onBackClick = {
                    navController.navigateUp() // 返回上一页
                },
                rightIconPainter = painterResource(R.mipmap.icon_add_grady),
                onRightActionClick = {
                    sizeViewModel.toggleAddDialog(true) // 显示添加对话框
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        SizeDragWidget(
            modifier = Modifier.padding(padding), data = sizes, onEditClick = { sizeEntity ->
                // 点击编辑按钮，更新当前选择的实体并显示底部弹窗
                sizeViewModel.updateEntity(sizeEntity)
                sizeViewModel.toggleRenameOrDeleteBottomSheet(true)
            }) { from, to, items ->
            // 拖动排序回调
            LogUtils.d("拖动 from: $from, to: $to ")
            sizeViewModel.updateSortOrders(items)
        }
    }

    // 重命名编辑对话框
    if (uiState.editDialog) {
        EditDialog(
            title = "重命名", value = uiState.entity?.name ?: "", // 当前实体的名称
            placeholder = "不能与已有名称重复", onDismissRequest = {
                sizeViewModel.toggleEditDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                sizeViewModel.toggleEditDialog(false)
                sizeViewModel.updateEntityDatabase(newName) // 更新数据库
            })
    }

    // 添加尺码对话框
    if (uiState.addDialog) {
        EditDialog(
            title = "添加", value = "", // 初始为空
            placeholder = "不能与已有名称重复", onDismissRequest = {
                sizeViewModel.toggleAddDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                sizeViewModel.insertWithAutoOrder(newName) // 插入新尺码
            })
    }

    // 重命名/删除底部弹窗
    RenameOrDeleteBottomSheet(
        visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
            sizeViewModel.toggleRenameOrDeleteBottomSheet(false) // 关闭弹窗
        }) { actionType ->
        sizeViewModel.toggleRenameOrDeleteBottomSheet(false)
        when (actionType) {
            RenameOrDeleteType.RENAME -> {
                sizeViewModel.toggleEditDialog(true) // 显示重命名对话框
            }

            RenameOrDeleteType.DELETE -> {
                sizeViewModel.deleteEntityDatabase() // 删除尺码
            }
        }
    }
}

/**
 * 尺码拖动部件
 * 显示尺码列表，支持拖动排序和编辑操作
 * @param modifier 修饰符
 * @param data 尺码实体列表
 * @param onEditClick 编辑按钮点击回调
 * @param onMove 拖动排序回调
 */
@Composable private fun SizeDragWidget(
    modifier: Modifier = Modifier, data: List<SizeEntity>, onEditClick: (SizeEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<SizeEntity>) -> Unit
) {
    DragLazyColumn(
        modifier = modifier, items = data.map { it.copy() }.toMutableList(), // 创建列表副本
        onMove = onMove, key = { it.id } // 使用尺码ID作为唯一标识
    ) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent
                    ) // 拖动时显示背景色
                    .height(64.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                // 尺码名称
                Text(
                    text = item.name, fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f)) // 填充剩余空间

                // 编辑按钮
                Image(
                    painter = painterResource(id = R.mipmap.icon_edit), contentDescription = "编辑", modifier = Modifier
                        .padding(7.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null // 禁用点击涟漪效果
                        ) {
                            onEditClick(item) // 点击编辑按钮回调
                        })

                // 拖动图标
                Image(
                    painter = painterResource(id = R.mipmap.icon_drag), contentDescription = "拖动排序", modifier = Modifier.padding(
                        start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp
                    )
                )
            }
        }
    }
}

/**
 * 尺码拖动部件的预览函数
 */
@Composable @Preview(showBackground = true) fun SizeDragWidgetPreview() {
    SizeDragWidget(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), data = defaultSizeData, // 使用默认尺码数据进行预览
        onEditClick = {}, onMove = { _, _, _ -> })
}