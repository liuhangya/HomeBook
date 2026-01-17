package com.fanda.homebook.closet

import android.util.Log
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
import com.fanda.homebook.data.color.ColorTypeViewModel
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

@Composable fun EditClosetColorPage(modifier: Modifier = Modifier, navController: NavController, colorTypeViewModel: ColorTypeViewModel = viewModel(factory = AppViewModelProvider.factory)) {

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
                navController.navigate(RoutePath.ClosetAddColor.route)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        ColorDragWidget(Modifier.padding(padding), colorList = colorTypes, onEditClick = {
            colorTypeViewModel.updateColor(it)
            colorTypeViewModel.toggleRenameOrDeleteBottomSheet(true)
        }) { from, to, items ->
            LogUtils.d("拖动 from: $from, to: $to ")
            colorTypeViewModel.updateSortOrders(from, to, items)
        }
    }

    if (uiState.editColorDialog) {
        EditDialog(title = "重命名", value = uiState.colorType?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            colorTypeViewModel.toggleAddOrEditColorDialog(false)
        }, onConfirm = {
            colorTypeViewModel.toggleAddOrEditColorDialog(false)
            colorTypeViewModel.updateColorTypeDatabase(it)
        })
    }
    RenameOrDeleteBottomSheet(visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
        colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
    }) {
        colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
        if (it == RenameOrDeleteType.RENAME) {
            colorTypeViewModel.toggleAddOrEditColorDialog(true)
        } else {
            colorTypeViewModel.deleteColorTypeDatabase()
        }
    }
}

@Composable private fun ColorDragWidget(
    modifier: Modifier = Modifier, colorList: List<ColorTypeEntity>, onEditClick: (ColorTypeEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<ColorTypeEntity>) -> Unit
) {
    DragLazyColumn(modifier = modifier, items = colorList.map { it.copy() }.toMutableList(), onMove = onMove) { item, isDragging ->
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

@Composable @Preview(showBackground = true) fun EditClosetColorPagePreview() {
    EditClosetColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}