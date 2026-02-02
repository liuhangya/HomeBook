package com.fanda.homebook.book

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fanda.homebook.R
import com.fanda.homebook.book.viewmodel.EditTransactionCategoryViewModel
import com.fanda.homebook.closet.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.closet.sheet.RenameOrDeleteType
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.tools.LogUtils
import kotlin.collections.forEach

@OptIn(ExperimentalLayoutApi::class) @SuppressLint("UnusedBoxWithConstraintsScope") @Composable fun EditTransactionCategoryPage(
    modifier: Modifier = Modifier, navController: NavController, viewModel: EditTransactionCategoryViewModel = viewModel(factory = AppViewModelProvider.factory)
) {

    val uiState by viewModel.uiState.collectAsState()
    val category by viewModel.category.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategory by viewModel.subCategory.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("subCategories: $subCategories")

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "分类管理",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "",
            backIconPainter = painterResource(R.mipmap.icon_back),
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .imePadding()   // 让输入法能顶起内容，不遮挡内容
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                ) {
                    categories.forEach {
                        SelectableRoundedButton(
                            text = it.name, selected = category?.name == it.name, onClick = { viewModel.updateCategory(it) })
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                EditCategoryGrid(items = subCategories, modifier = Modifier.padding(top = 8.dp, start = 33.dp, end = 33.dp)) {
                    viewModel.updateSubCategory(it)
                    if (it == null) {
                        // 添加操作
                        viewModel.toggleAddDialog(true)
                    } else {
                        // 删除或修改当前分类操作
                        viewModel.toggleDeleteOrEditBottomSheet(true)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // 底部间距
            }
        }

        if (uiState.addDialog) {
            EditDialog(title = "添加分类", value = "", placeholder = "不能与已有分类重复", onDismissRequest = {
                viewModel.toggleAddDialog(false)
            }, onConfirm = {
                viewModel.insertWithAutoOrder(it)
            })
        }

        RenameOrDeleteBottomSheet(visible = uiState.deleteOrEditDialog, onDismiss = {
            viewModel.toggleDeleteOrEditBottomSheet(false)
        }) {
            viewModel.toggleDeleteOrEditBottomSheet(false)
            if (it == RenameOrDeleteType.RENAME) {
                viewModel.toggleEditDialog(true)
            } else {
                viewModel.deleteEntityDatabase()
            }
        }

        if (uiState.editDialog) {
            EditDialog(title = "重命名", value = subCategory?.name ?: "", placeholder = "不能与已有名称重复", onDismissRequest = {
                viewModel.toggleEditDialog(false)
            }, onConfirm = {
                viewModel.toggleEditDialog(false)
                viewModel.updateEntityDatabase(it)
            })
        }

    }

}


@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun EditCategoryGrid(
    modifier: Modifier = Modifier, items: List<TransactionSubEntity>?, onItemClick: (TransactionSubEntity?) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        val itemSpacing = 26.dp
        val maxColumns = 4
        val horizontalPadding = 33.dp * 2 // 两侧的 padding 总和

        // 正确的计算公式：可用宽度 = 总宽度 - 左右padding - (列间距总和)
        val totalHorizontalSpacing = itemSpacing * (maxColumns - 1)
        // 因为 Item 加了 5 的padding ，5*4*2 间距，不知道为什么还要加2dp才行
        val itemWidth = (maxWidth - horizontalPadding - totalHorizontalSpacing -42.dp) / maxColumns

        // padding 要加在这里才正常，不要加到父容器中
        FlowRow(
            modifier = modifier
                .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing), verticalArrangement = Arrangement.spacedBy(15.dp), maxItemsInEachRow = maxColumns
        ) {
            items?.forEach { category ->
                EditCategoryItem(itemWidth = itemWidth, category = category, onItemClick = onItemClick)
            }
            val category = TransactionSubEntity(name = "添加", type = TransactionType.ADD.type, sortOrder = 1000, categoryId = 0)
            EditCategoryItem(itemWidth = itemWidth, category = category, onItemClick = { onItemClick(null) })

        }
    }
}

@Composable fun EditCategoryItem(
    modifier: Modifier = Modifier, itemWidth: Dp, category: TransactionSubEntity, onItemClick: (TransactionSubEntity) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onItemClick(category)
            }
            .padding(5.dp)) {
        // 通过 colorFilter 来改变图标颜色
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(itemWidth)
                .clip(
                    CircleShape
                )
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = getCategoryIcon(category.type)), contentDescription = null, modifier = Modifier.scale(1f)
            )

        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name, fontSize = 14.sp, style = TextStyle.Default, color = colorResource(R.color.color_333333), modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}