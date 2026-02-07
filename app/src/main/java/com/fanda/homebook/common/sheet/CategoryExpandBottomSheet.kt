package com.fanda.homebook.common.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.hjq.toast.Toaster

/**
 * 分类可展开底部弹窗组件
 * 用于显示两级分类（一级分类和二级子分类）的选择界面
 *
 * @param categories 分类数据列表（包含一级分类及其子分类）
 * @param categoryEntity 当前选中的一级分类实体（可为空）
 * @param subCategoryEntity 当前选中的二级子分类实体（可为空）
 * @param visible 弹窗是否可见的函数
 * @param onDismiss 弹窗关闭回调函数
 * @param onSettingClick 设置按钮点击回调（用于跳转到分类编辑页面，可选）
 * @param onConfirm 确认选择回调函数，返回选中的一级和二级分类
 */
@Composable fun CategoryExpandBottomSheet(
    categories: List<CategoryWithSubCategories>,
    categoryEntity: CategoryEntity?,
    subCategoryEntity: SubCategoryEntity?,
    visible: () -> Boolean,
    onDismiss: () -> Unit,
    onSettingClick: (() -> Unit)? = null,
    onConfirm: (CategoryEntity?, SubCategoryEntity?) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        // 记录每一个一级分类是否展开其二级分类
        // 默认展开当前选中子分类所在的一级分类
        var expandedMap by remember {
            mutableStateOf(categories.associate { it.category.id to (it.category.id == subCategoryEntity?.categoryId) }.toMutableMap())
        }

        // 记录当前选中的分类（包括一级和二级）
        var selectedCategory by remember { mutableStateOf(categoryEntity) }
        var selectedSubCategory by remember { mutableStateOf(subCategoryEntity) }

        Column(modifier = Modifier.fillMaxWidth()) {
            // 弹窗标题栏
            SheetTitleWidget(
                title = "分类", onSettingClick = onSettingClick
            ) {
                // 确认按钮点击逻辑
                if (selectedCategory != null && selectedSubCategory == null && categories.find { it.category.id == selectedCategory?.id && it.subCategories.isNotEmpty() } != null) {
                    // 一级分类有子分类但未选择子分类时提示
                    Toaster.show("请选择子分类")
                } else {
                    // 确认选择
                    selectedCategory?.let {
                        onConfirm(selectedCategory, selectedSubCategory)
                        onDismiss()
                    }
                }
            }

            // 分类列表
            LazyColumn(contentPadding = PaddingValues(bottom = 10.dp)) {
                items(categories, key = { it.category.id }) { category ->
                    ExpandableCategoryItem(category = category, isExpanded = expandedMap[category.category.id] == true, isSelected = selectedCategory?.id == category.category.id, onToggleExpand = {
                        // 切换一级分类时，更新选中状态
                        if (it.id != selectedCategory?.id) {
                            selectedCategory = category.category
                            selectedSubCategory = null
                        }
                        // 切换展开/收起状态
                        expandedMap = expandedMap.toMutableMap().apply {
                            put(category.category.id, !getOrDefault(category.category.id, false))
                        }
                    }, onSubClick = { sub ->
                        // 点击二级子分类
                        selectedSubCategory = sub
                    }, isSelectedSub = { sub -> sub.id == selectedSubCategory?.id })
                }
            }
        }
    }
}

/**
 * 可展开的分类项组件
 * 显示单个一级分类及其可展开的二级子分类列表
 *
 * @param category 分类数据（包含一级分类和其子分类）
 * @param isExpanded 当前是否展开子分类列表
 * @param isSelected 当前是否选中该一级分类
 * @param onToggleExpand 一级分类点击回调（切换展开状态）
 * @param onSubClick 二级子分类点击回调
 * @param isSelectedSub 判断二级子分类是否被选中的函数
 * @param modifier Compose修饰符
 */
@Composable fun ExpandableCategoryItem(
    category: CategoryWithSubCategories,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggleExpand: (CategoryEntity) -> Unit,
    onSubClick: (SubCategoryEntity) -> Unit,
    isSelectedSub: (SubCategoryEntity) -> Boolean,
    modifier: Modifier = Modifier
) {
    // 箭头旋转动画（展开时旋转90度）
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f, animationSpec = tween(durationMillis = 200, easing = LinearEasing), label = "expandArrowRotation"
    )

    Column(modifier = modifier) {
        // 一级分类项
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand(category.category) }
            .padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            // 分类名称
            Text(
                text = category.category.name,
                fontSize = 16.sp,
                color = Color.Black,
            )

            // 如果该分类有子分类，显示可展开箭头
            if (category.subCategories.isNotEmpty()) {
                Image(
                    painter = painterResource(R.mipmap.icon_right),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .rotate(rotation)  // 根据展开状态旋转箭头
                )
            }

            // 占位空间
            Spacer(Modifier.weight(1f))

            // 选中状态指示器
            if (isSelected) {
                Image(
                    painter = painterResource(R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(16.dp)
                )
            }
        }

        // 二级子分类列表（带动画展开/收起效果）
        AnimatedVisibility(
            visible = isExpanded && category.subCategories.isNotEmpty(), enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column {
                category.subCategories.forEach { sub ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubClick(sub) }
                        .padding(start = 64.dp, top = 16.dp, bottom = 16.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        // 子分类名称
                        Text(
                            text = sub.name,
                            fontSize = 16.sp,
                            color = Color.Black,
                        )

                        // 占位空间
                        Spacer(Modifier.weight(1f))

                        // 选中状态指示器
                        if (isSelectedSub(sub)) {
                            Image(
                                painter = painterResource(R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}