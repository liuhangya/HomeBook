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
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.PayWayViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.pay.PayWayEntity
import com.fanda.homebook.tools.LogUtils

@Composable fun EditPayWayPage(modifier: Modifier = Modifier, navController: NavController,payWayViewModel: PayWayViewModel = viewModel(factory = AppViewModelProvider.factory)) {

    // 通过 ViewModel 状态管理进行数据绑定
    val payWays by payWayViewModel.payWays.collectAsState()

    val uiState by payWayViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "支付方式管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                payWayViewModel.toggleAddDialog(true)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        PayWayDragWidget(Modifier.padding(padding), data = payWays, onEditClick = {
            payWayViewModel.updateEntity(it)
            payWayViewModel.toggleRenameOrDeleteBottomSheet(true)
        }) { from, to, items ->
            LogUtils.d("拖动 from: $from, to: $to ")
            payWayViewModel.updateSortOrders(items)
        }
    }

    if (uiState.editDialog) {
        EditDialog(title = "重命名", value = uiState.entity?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            payWayViewModel.toggleEditDialog(false)
        }, onConfirm = {
            payWayViewModel.toggleEditDialog(false)
            payWayViewModel.updateEntityDatabase(it)
        })
    }

    if (uiState.addDialog) {
        EditDialog(title = "添加", value = "", placeholder = "不能与已有名称重复", onDismissRequest = {
            payWayViewModel.toggleAddDialog(false)
        }, onConfirm = {
            payWayViewModel.insertWithAutoOrder(it)
        })
    }
    RenameOrDeleteBottomSheet(visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
        payWayViewModel.toggleRenameOrDeleteBottomSheet(false)
    }) {
        payWayViewModel.toggleRenameOrDeleteBottomSheet(false)
        if (it == RenameOrDeleteType.RENAME) {
            payWayViewModel.toggleEditDialog(true)
        } else {
            payWayViewModel.deleteEntityDatabase()
        }
    }
}

@Composable private fun PayWayDragWidget(
    modifier: Modifier = Modifier, data: List<PayWayEntity>, onEditClick: (PayWayEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<PayWayEntity>) -> Unit
) {
    DragLazyColumn(modifier = modifier, items = data.map { it.copy() }.toMutableList(), onMove = onMove, key = { it.id }) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.name, fontSize = 16.sp, color = Color.Black,modifier = Modifier.padding(start = 16.dp, end = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.mipmap.icon_edit),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(7.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onEditClick(item)
                        })

                Image(
                    painter = painterResource(id = R.mipmap.icon_drag), contentDescription = null, modifier = Modifier.padding(start = 7.dp, top = 7.dp, end = 22.dp, bottom = 7.dp)
                )

            }
        }
    }
}
