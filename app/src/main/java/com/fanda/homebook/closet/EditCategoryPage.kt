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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.navArgument
import com.fanda.homebook.R
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.common.viewmodel.CategoryViewModel
import com.fanda.homebook.components.DragLazyColumn
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/*
*
* 衣橱页面
* */
@Composable fun EditCategoryPage(modifier: Modifier = Modifier, navController: NavController, categoryViewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.factory)) {

    val categories by categoryViewModel.categories.collectAsState()

    val uiState by categoryViewModel.uiState.collectAsState()

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "分类管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightIconPainter = painterResource(R.mipmap.icon_add_grady),
            onRightActionClick = {
                categoryViewModel.toggleAddDialog(true)
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        CategoryDragWidget(Modifier.padding(padding), data = categories, onEditClick = {
            categoryViewModel.updateEntity(it)
            categoryViewModel.toggleRenameOrDeleteBottomSheet(true)
        }, onItemClick = {
            navController.navigate("${RoutePath.EditSubCategory.route}?categoryId=${it.id}&categoryName=${it.name}")
        }) { from, to, items ->
            LogUtils.d("拖动 from: $from, to: $to ")
            categoryViewModel.updateSortOrders(items)
        }
    }

    if (uiState.editDialog) {
        EditDialog(title = "重命名", value = uiState.entity?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
            categoryViewModel.toggleEditDialog(false)
        }, onConfirm = {
            categoryViewModel.toggleEditDialog(false)
            categoryViewModel.updateEntityDatabase(it)
        })
    }

    if (uiState.addDialog) {
        EditDialog(title = "添加", value = "", placeholder = "不能与已有名称重复", onDismissRequest = {
            categoryViewModel.toggleAddDialog(false)
        }, onConfirm = {
            categoryViewModel.insertWithAutoOrder(it)
        })
    }
    RenameOrDeleteBottomSheet(visible = uiState.renameOrDeleteBottomSheet, onDismiss = {
        categoryViewModel.toggleRenameOrDeleteBottomSheet(false)
    }) {
        categoryViewModel.toggleRenameOrDeleteBottomSheet(false)
        if (it == RenameOrDeleteType.RENAME) {
            categoryViewModel.toggleEditDialog(true)
        } else {
            categoryViewModel.deleteEntityDatabase()
        }
    }
}


@Composable private fun CategoryDragWidget(
    modifier: Modifier = Modifier,
    data: List<CategoryEntity>,
    onEditClick: (CategoryEntity) -> Unit,
    onItemClick: (CategoryEntity) -> Unit,
    onMove: (from: Int, to: Int, items: MutableList<CategoryEntity>) -> Unit
) {
    data.forEach {
        LogUtils.d("item: $it")
    }
    DragLazyColumn(modifier = modifier, items = data.map { it.copy() }.toMutableList(), onMove = onMove, key = { it.id }) { item, isDragging ->
        GradientRoundedBoxWithStroke {
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { onItemClick(item) }
                .background(if (isDragging) colorResource(id = R.color.color_F5F5F5) else Color.Transparent)
                .height(64.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.name, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp, end = 8.dp), color = Color.Black)
                Image(
                    painter = painterResource(id = R.mipmap.icon_right), contentDescription = null, modifier = Modifier.size(4.dp, 8.dp), colorFilter = ColorFilter.tint(Color.Black)
                )
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
