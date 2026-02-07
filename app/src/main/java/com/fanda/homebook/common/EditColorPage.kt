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
import com.fanda.homebook.common.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.common.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.ColorTypeViewModel
import com.fanda.homebook.components.ColoredCircleWithBorder
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.color.defaultColorData
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/**
 * 编辑颜色页面（颜色管理页面）
 * 用于展示所有颜色列表，支持拖动排序、编辑、删除等操作
 * @param modifier 修饰符
 * @param navController 导航控制器，用于页面跳转
 * @param colorTypeViewModel 颜色类型ViewModel，由Compose自动注入
 */
@Composable fun EditColorPage(
    modifier: Modifier = Modifier, navController: NavController, colorTypeViewModel: ColorTypeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    // 收集颜色类型列表数据
    val colorTypes by colorTypeViewModel.colorTypes.collectAsState()

    // 收集UI状态
    val uiState by colorTypeViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "颜色管理",
                onBackClick = {
                    navController.navigateUp() // 点击返回按钮返回上一页
                },
                rightIconPainter = painterResource(R.mipmap.icon_add_grady),
                onRightActionClick = {
                    // 点击添加按钮，跳转到添加颜色页面，colorId=-1表示添加模式
                    navController.navigate("${RoutePath.AddColor.route}?colorId=-1")
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        // 颜色拖动列表组件
        ColorDragWidget(
            modifier = Modifier.padding(padding), colorList = colorTypes, onEditClick = { colorEntity ->
                // 点击编辑按钮，更新当前选择的实体并显示底部弹窗
                colorTypeViewModel.updateEntity(colorEntity)
                colorTypeViewModel.toggleRenameOrDeleteBottomSheet(true)
            }) { from, to, items ->
            // 拖动排序回调
            LogUtils.d("拖动 from: $from, to: $to ")
            colorTypeViewModel.updateSortOrders(items)
        }
    }

    // 重命名/删除底部弹窗
    RenameOrDeleteBottomSheet(
        firstMenuText = "编辑", visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
            // 关闭弹窗
            colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
        }) { actionType ->
        colorTypeViewModel.toggleRenameOrDeleteBottomSheet(false)
        when (actionType) {
            RenameOrDeleteType.RENAME -> {
                // 点击编辑，跳转到添加颜色页面（编辑模式）
                navController.navigate("${RoutePath.AddColor.route}?colorId=${uiState.entity?.id}")
            }

            RenameOrDeleteType.DELETE -> {
                // 点击删除，从数据库删除颜色
                colorTypeViewModel.deleteEntityDatabase()
            }
        }
    }
}

/**
 * 颜色拖动部件
 * 显示颜色列表，支持拖动排序和编辑操作
 * @param modifier 修饰符
 * @param colorList 颜色实体列表
 * @param onEditClick 编辑按钮点击回调
 * @param onMove 拖动排序回调
 */
@Composable private fun ColorDragWidget(
    modifier: Modifier = Modifier, colorList: List<ColorTypeEntity>, onEditClick: (ColorTypeEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<ColorTypeEntity>) -> Unit
) {
    DragLazyColumn(
        modifier = modifier, items = colorList.map { it.copy() }.toMutableList(), // 创建列表副本，避免直接修改原始数据
        onMove = onMove, key = { it.id } // 使用颜色ID作为唯一标识
    ) { item, isDragging ->
        // 每个颜色项的UI
        GradientRoundedBoxWithStroke {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent
                    ) // 拖动时显示背景色
                    .height(64.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧颜色圆圈
                ColoredCircleWithBorder(
                    color = Color(item.color), modifier = Modifier.padding(start = 16.dp, end = 12.dp)
                )

                // 颜色名称
                Text(text = item.name, fontSize = 16.sp, color = Color.Black)

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
 * 颜色拖动部件的预览函数
 */
@Composable @Preview(showBackground = true) fun ColorDragWidgetPreview() {
    ColorDragWidget(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), colorList = defaultColorData, // 使用默认颜色数据进行预览
        onEditClick = {}, onMove = { _, _, _ -> })
}

/**
 * 编辑颜色页面的预览函数
 */
@Composable @Preview(showBackground = true) fun EditColorPagePreview() {
    EditColorPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}