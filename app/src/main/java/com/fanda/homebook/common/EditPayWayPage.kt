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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.common.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.PayWayViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.tools.LogUtils

/**
 * 编辑支付方式页面
 * 用于管理支付方式的CRUD操作和排序
 * @param modifier 修饰符
 * @param navController 导航控制器，用于页面跳转
 * @param payWayViewModel 支付方式ViewModel，由Compose自动注入
 */
@Composable fun EditPayWayPage(
    modifier: Modifier = Modifier, navController: NavController, payWayViewModel: PayWayViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    // 通过ViewModel状态管理进行数据绑定
    val payWays by payWayViewModel.payWays.collectAsState()

    val uiState by payWayViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "支付方式管理",
                onBackClick = {
                    navController.navigateUp() // 返回上一页
                },
                rightIconPainter = painterResource(R.mipmap.icon_add_grady),
                onRightActionClick = {
                    payWayViewModel.toggleAddDialog(true) // 显示添加对话框
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        PayWayDragWidget(
            modifier = Modifier.padding(padding), data = payWays, onEditClick = { payWayEntity ->
                // 点击编辑按钮，更新当前选择的实体并显示底部弹窗
                payWayViewModel.updateEntity(payWayEntity)
                payWayViewModel.toggleRenameOrDeleteBottomSheet(true)
            }) { from, to, items ->
            // 拖动排序回调
            LogUtils.d("拖动 from: $from, to: $to ")
            payWayViewModel.updateSortOrders(items)
        }
    }

    // 重命名编辑对话框
    if (uiState.editDialog) {
        EditDialog(
            title = "重命名", value = uiState.entity?.name ?: "", // 当前实体的名称
            placeholder = "不能与已有名称重复", onDismissRequest = {
                payWayViewModel.toggleEditDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                payWayViewModel.toggleEditDialog(false)
                payWayViewModel.updateEntityDatabase(newName) // 更新数据库
            })
    }

    // 添加支付方式对话框
    if (uiState.addDialog) {
        EditDialog(
            title = "添加", value = "", // 初始为空
            placeholder = "不能与已有名称重复", onDismissRequest = {
                payWayViewModel.toggleAddDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                payWayViewModel.insertWithAutoOrder(newName) // 插入新支付方式
            })
    }

    // 重命名/删除底部弹窗
    RenameOrDeleteBottomSheet(
        visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
            payWayViewModel.toggleRenameOrDeleteBottomSheet(false) // 关闭弹窗
        }) { actionType ->
        payWayViewModel.toggleRenameOrDeleteBottomSheet(false)
        when (actionType) {
            RenameOrDeleteType.RENAME -> {
                payWayViewModel.toggleEditDialog(true) // 显示重命名对话框
            }

            RenameOrDeleteType.DELETE -> {
                payWayViewModel.deleteEntityDatabase() // 删除支付方式
            }
        }
    }
}

/**
 * 支付方式拖动部件
 * 显示支付方式列表，支持拖动排序和编辑操作
 * @param modifier 修饰符
 * @param data 支付方式实体列表
 * @param onEditClick 编辑按钮点击回调
 * @param onMove 拖动排序回调
 */
@Composable private fun PayWayDragWidget(
    modifier: Modifier = Modifier, data: List<PayWayEntity>, onEditClick: (PayWayEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<PayWayEntity>) -> Unit
) {
    DragLazyColumn(
        modifier = modifier, items = data.map { it.copy() }.toMutableList(), // 创建列表副本
        onMove = onMove, key = { it.id } // 使用支付方式ID作为唯一标识
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
                // 支付方式名称
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