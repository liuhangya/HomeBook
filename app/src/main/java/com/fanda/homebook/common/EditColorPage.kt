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
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.data.color.defaultColorData
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

@Composable fun EditColorPage(modifier: Modifier = Modifier, navController: NavController, colorTypeViewModel: ColorTypeViewModel = viewModel(factory = AppViewModelProvider.factory)) {

    // 通过 ViewModel 状态管理进行数据绑定
    val colorTypes by colorTypeViewModel.colorTypes.collectAsState()

    val uiState by colorTypeViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "颜色管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                navController.navigate(RoutePath.AddColor.route)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        ColorDragWidget(Modifier.padding(padding), colorList = colorTypes, onEditClick = {
            colorTypeViewModel.updateEntity(it)
            colorTypeViewModel.toggleRenameOrDeleteBottomSheet(true)
        }) { from, to, items ->
            LogUtils.d("拖动 from: $from, to: $to ")
            colorTypeViewModel.updateSortOrders(items)
        }
    }

    if (uiState.editDialog) {
        EditDialog(title = "重命名", value = uiState.entity?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            colorTypeViewModel.toggleEditDialog(false)
        }, onConfirm = {
            colorTypeViewModel.toggleEditDialog(false)
            colorTypeViewModel.updateEntityDatabase(it)
        })
    }
    RenameOrDeleteBottomSheet(visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
        colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
    }) {
        colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
        if (it == RenameOrDeleteType.RENAME) {
            colorTypeViewModel.toggleEditDialog(true)
        } else {
            colorTypeViewModel.deleteEntityDatabase()
        }
    }
}

@Composable private fun ColorDragWidget(
    modifier: Modifier = Modifier, colorList: List<ColorTypeEntity>, onEditClick: (ColorTypeEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<ColorTypeEntity>) -> Unit
) {
    DragLazyColumn(modifier = modifier, items = colorList.map { it.copy() }.toMutableList(), onMove = onMove, key = { it.id }) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredCircleWithBorder(color = Color(item.color), modifier = Modifier.padding(start = 16.dp, end = 12.dp))
                Text(text = item.name, fontSize = 16.sp, color = Color.Black)
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

@Composable @Preview(showBackground = true) fun ColorDragWidgetPreview() {
    ColorDragWidget(modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding(), colorList = defaultColorData, onEditClick = {}, onMove = { _, _, _ -> })
}

@Composable @Preview(showBackground = true) fun EditColorPagePreview() {
    EditColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}