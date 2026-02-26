package com.fanda.homebook.book

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.fanda.homebook.common.sheet.RenameOrDeleteBottomSheet
import com.fanda.homebook.common.sheet.RenameOrDeleteType
import com.fanda.homebook.components.EditDialog
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.transaction.TransactionSubEntity
import com.fanda.homebook.data.transaction.TransactionType
import com.fanda.homebook.quick.ui.getCategoryIcon
import com.fanda.homebook.tools.LogUtils

@OptIn(ExperimentalLayoutApi::class) @SuppressLint("UnusedBoxWithConstraintsScope") @Composable fun EditTransactionCategoryPage(
    modifier: Modifier = Modifier, navController: NavController, viewModel: EditTransactionCategoryViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 收集状态
    val uiState by viewModel.uiState.collectAsState()
    val category by viewModel.category.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategory by viewModel.subCategory.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()

    LogUtils.d("uiState: $uiState")
    LogUtils.d("subCategories: $subCategories")

    // 通过 statusBarsPadding 单独加padding，让弹窗背景占满全屏
    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
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
            // 主分类切换区域
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
                    // 显示所有主分类按钮
                    categories.forEach { mainCategory ->
                        SelectableRoundedButton(
                            text = mainCategory.name, selected = category?.name == mainCategory.name, onClick = { viewModel.updateCategory(mainCategory) })
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // 子分类网格区域
            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                EditCategoryGrid(
                    items = subCategories, modifier = Modifier.padding(top = 8.dp, start = 33.dp, end = 33.dp)
                ) { clickedCategory ->
                    viewModel.updateSubCategory(clickedCategory)
                    if (clickedCategory == null) {
                        // 添加操作：点击"添加"按钮
                        viewModel.toggleAddDialog(true)
                    } else {
                        // 编辑/删除操作：点击已有分类
                        viewModel.toggleDeleteOrEditBottomSheet(true)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp)) // 底部间距
            }
        }

        // 添加分类对话框
        if (uiState.addDialog) {
            EditDialog(title = "添加分类", value = "", placeholder = "不能与已有分类重复", onDismissRequest = {
                viewModel.toggleAddDialog(false)
            }, onConfirm = {
                viewModel.insertWithAutoOrder(it)
            })
        }

        // 重命名或删除底部弹窗
        RenameOrDeleteBottomSheet(
            visible = uiState.deleteOrEditDialog, onDismiss = {
                viewModel.toggleDeleteOrEditBottomSheet(false)
            }) { actionType ->
            viewModel.toggleDeleteOrEditBottomSheet(false)
            if (actionType == RenameOrDeleteType.RENAME) {
                // 重命名操作
                viewModel.toggleEditDialog(true)
            } else {
                // 删除操作
                viewModel.deleteEntityDatabase()
            }
        }

        // 编辑分类对话框
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

/**
 * 编辑分类网格组件
 * 以网格形式显示子分类，支持动态计算布局
 *
 * @param modifier 修饰符
 * @param items 子分类列表
 * @param onItemClick 分类项点击回调，null表示点击"添加"按钮
 */
@SuppressLint("UnusedBoxWithConstraintsScope") @OptIn(ExperimentalLayoutApi::class) @Composable fun EditCategoryGrid(
    modifier: Modifier = Modifier, items: List<TransactionSubEntity>?, onItemClick: (TransactionSubEntity?) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
//            .animateContentSize()  // 内容变化时添加动画
    ) {
        // 布局参数
        val itemSpacing = 26.dp       // 分类项之间的间距
        val maxColumns = 4            // 最大列数
        val horizontalPadding = 33.dp * 2 // 两侧的padding总和（左右各33dp）
        val itemInternalPadding = 5.dp * 4 * 2 // 分类项内部padding计算
        val extraSpacing = 2.dp       // 额外间距（经验值）

        // 计算列间距总和
        val totalHorizontalSpacing = itemSpacing * (maxColumns - 1)

        // 计算每个分类项的宽度：可用宽度 / 列数
        // 可用宽度 = 总宽度 - 左右padding - 列间距总和 - 内部padding - 额外间距
        val itemWidth = (maxWidth - horizontalPadding - totalHorizontalSpacing - itemInternalPadding - extraSpacing) / maxColumns

        // 流式布局（自动换行）
        FlowRow(
            modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(itemSpacing),  // 水平间距
            verticalArrangement = Arrangement.spacedBy(15.dp),         // 垂直间距
            maxItemsInEachRow = maxColumns                              // 每行最大项数
        ) {
            // 显示已有分类项
            items?.forEach { category ->
                EditCategoryItem(
                    itemWidth = itemWidth, category = category, onItemClick = onItemClick
                )
            }

            // 添加"添加"分类项
            val addCategory = TransactionSubEntity(
                name = "添加", type = TransactionType.ADD.type,  // 特殊类型，显示加号图标
                sortOrder = 1000,                  // 排序值最大，显示在最后
                categoryId = 0
            )
            EditCategoryItem(itemWidth = itemWidth, category = addCategory, onItemClick = { onItemClick(null) }  // 点击"添加"按钮时传null
            )
        }
    }
}

/**
 * 单个分类项组件
 *
 * @param modifier 修饰符
 * @param itemWidth 分类项宽度（动态计算）
 * @param category 分类实体
 * @param onItemClick 点击回调
 */
@Composable fun EditCategoryItem(
    modifier: Modifier = Modifier, itemWidth: Dp, category: TransactionSubEntity, onItemClick: (TransactionSubEntity) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))  // 圆角矩形
            .clickable {
                onItemClick(category)
            }
            .padding(5.dp)  // 内部padding
    ) {
        // 分类图标容器
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(itemWidth)          // 动态宽度
                .clip(CircleShape)        // 圆形
                .background(Color.White)  // 白色背景
        ) {
            // 分类图标（根据分类类型显示不同图标）
            Image(
                painter = painterResource(id = getCategoryIcon(category.type)), contentDescription = null, modifier = Modifier.scale(1f)  // 原始大小
            )
        }

        Spacer(modifier = Modifier.height(4.dp))  // 图标和文字之间的间距

        // 分类名称
        Text(
            text = category.name, fontSize = 14.sp, style = TextStyle.Default, color = colorResource(R.color.color_333333),  // 深灰色文字
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}