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
import com.fanda.homebook.common.viewmodel.ProductViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.product.defaultProductData
import com.fanda.homebook.tools.LogUtils

/**
 * 编辑品牌（产品）页面
 * 用于管理品牌/产品的CRUD操作和排序
 * @param modifier 修饰符
 * @param navController 导航控制器，用于页面跳转
 * @param productViewModel 产品ViewModel，由Compose自动注入
 */
@Composable fun EditProductPage(
    modifier: Modifier = Modifier, navController: NavController, productViewModel: ProductViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    // 通过ViewModel状态管理进行数据绑定
    val products by productViewModel.products.collectAsState()

    val uiState by productViewModel.uiState.collectAsState()

    LogUtils.d("状态对象： $uiState")

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "品牌管理",
                onBackClick = {
                    navController.navigateUp() // 返回上一页
                },
                rightIconPainter = painterResource(R.mipmap.icon_add_grady),
                onRightActionClick = {
                    productViewModel.toggleAddDialog(true) // 显示添加对话框
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        ProductDragWidget(
            modifier = Modifier.padding(padding), data = products, onEditClick = { productEntity ->
                // 点击编辑按钮，更新当前选择的实体并显示底部弹窗
                productViewModel.updateEntity(productEntity)
                productViewModel.toggleRenameOrDeleteBottomSheet(true)
            }) { from, to, items ->
            // 拖动排序回调
            LogUtils.d("拖动 from: $from, to: $to ")
            productViewModel.updateSortOrders(items)
        }
    }

    // 重命名编辑对话框
    if (uiState.editDialog) {
        EditDialog(
            title = "重命名", value = uiState.entity?.name ?: "", // 当前实体的名称
            placeholder = "不能与已有名称重复", onDismissRequest = {
                productViewModel.toggleEditDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                productViewModel.toggleEditDialog(false)
                productViewModel.updateEntityDatabase(newName) // 更新数据库
            })
    }

    // 添加品牌对话框
    if (uiState.addDialog) {
        EditDialog(
            title = "添加", value = "", // 初始为空
            placeholder = "不能与已有名称重复", onDismissRequest = {
                productViewModel.toggleAddDialog(false) // 关闭对话框
            }, onConfirm = { newName ->
                productViewModel.insertWithAutoOrder(newName) // 插入新品牌
            })
    }

    // 重命名/删除底部弹窗
    RenameOrDeleteBottomSheet(
        visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
            productViewModel.toggleRenameOrDeleteBottomSheet(false) // 关闭弹窗
        }) { actionType ->
        productViewModel.toggleRenameOrDeleteBottomSheet(false)
        when (actionType) {
            RenameOrDeleteType.RENAME -> {
                productViewModel.toggleEditDialog(true) // 显示重命名对话框
            }

            RenameOrDeleteType.DELETE -> {
                productViewModel.deleteEntityDatabase() // 删除品牌
            }
        }
    }
}

/**
 * 品牌拖动部件
 * 显示品牌列表，支持拖动排序和编辑操作
 * @param modifier 修饰符
 * @param data 品牌实体列表
 * @param onEditClick 编辑按钮点击回调
 * @param onMove 拖动排序回调
 */
@Composable private fun ProductDragWidget(
    modifier: Modifier = Modifier, data: List<ProductEntity>, onEditClick: (ProductEntity) -> Unit, onMove: (from: Int, to: Int, items: MutableList<ProductEntity>) -> Unit
) {
    DragLazyColumn(
        modifier = modifier, items = data.map { it.copy() }.toMutableList(), // 创建列表副本
        onMove = onMove, key = { it.id } // 使用品牌ID作为唯一标识
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
                // 品牌名称
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
 * 品牌拖动部件的预览函数
 */
@Composable @Preview(showBackground = true) fun ProductDragWidgetPreview() {
    ProductDragWidget(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), data = defaultProductData, // 使用默认品牌数据进行预览
        onEditClick = {}, onMove = { _, _, _ -> })
}