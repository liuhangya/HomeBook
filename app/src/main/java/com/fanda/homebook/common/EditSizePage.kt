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
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.SizeViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.data.size.defaultSizeData
import com.fanda.homebook.tools.LogUtils

@Composable fun EditSizePage(modifier: Modifier = Modifier, navController: NavController, sizeViewModel: SizeViewModel = viewModel(factory = AppViewModelProvider.factory)) {

    // 通过 ViewModel 状态管理进行数据绑定
    val sizes by sizeViewModel.sizes.collectAsState()

    val uiState by sizeViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "尺码管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                sizeViewModel.toggleAddDialog(true)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        SizeDragWidget(Modifier.padding(padding), data = sizes, onEditClick = {
            sizeViewModel.updateEntity(it)
            sizeViewModel.toggleRenameOrDeleteBottomSheet(true)
        }) { from, to, items ->
            LogUtils.d("拖动 from: $from, to: $to ")
            sizeViewModel.updateSortOrders(items)
        }
    }

    if (uiState.editDialog) {
        EditDialog(title = "重命名", value = uiState.entity?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            sizeViewModel.toggleEditDialog(false)
        }, onConfirm = {
            sizeViewModel.toggleEditDialog(false)
            sizeViewModel.updateEntityDatabase(it)
        })
    }

    if (uiState.addDialog) {
        EditDialog(title = "添加", value = "", placeholder = "不能与已有名称重复", onDismissRequest = {
            sizeViewModel.toggleAddDialog(false)
        }, onConfirm = {
            sizeViewModel.insertWithAutoOrder(it)
        })
    }
    RenameOrDeleteBottomSheet(visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
        sizeViewModel.toggleRenameOrDeleteBottomSheet(false)
    }) {
        sizeViewModel.toggleRenameOrDeleteBottomSheet(false)
        if (it == RenameOrDeleteType.RENAME) {
            sizeViewModel.toggleEditDialog(true)
        } else {
            sizeViewModel.deleteEntityDatabase()
        }
    }
}

@Composable private fun SizeDragWidget(
    modifier: Modifier = Modifier, data: List<SizeEntity>, onEditClick: (SizeEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<SizeEntity>) -> Unit
) {
    DragLazyColumn(modifier = modifier, items = data.map { it.copy() }.toMutableList(), onMove = onMove) { item, isDragging ->
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

@Composable @Preview(showBackground = true) fun SizeDragWidgetPreview() {
    SizeDragWidget(modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding(), data = defaultSizeData, onEditClick = {}, onMove = { _, _, _ -> })
}
