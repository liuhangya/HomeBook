package com.fanda.homebook.quick.sheet

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanda.homebook.R
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.data.LocalDataSource
import com.fanda.homebook.data.category.CategoryEntity
import com.fanda.homebook.data.category.CategoryWithSubCategories
import com.fanda.homebook.data.category.SubCategoryEntity
import com.hjq.toast.Toaster

data class Category(
    val id: String, val name: String, val children: List<SubCategory> = emptyList()
)

data class SubCategory(
    val id: String, val name: String, var selected: Boolean = false
)

data class SelectedCategory(
    val categoryId: String, val categoryName: String, val subCategoryId: String, val subCategoryName: String
)

@Composable fun ExpandableCategoryItem(
    category: Category, isExpanded: Boolean, isSelected: Boolean, onToggleExpand: () -> Unit, onSubClick: (SubCategory) -> Unit, isSelectedSub: (SubCategory) -> Boolean, modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f, animationSpec = tween(durationMillis = 200, easing = LinearEasing), label = "expandArrowRotation"
    )

    Column(modifier = modifier) {
        // 一级分类项
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.name,
                fontSize = 16.sp,
                color = Color.Black,
            )
            Image(
                painter = painterResource(R.mipmap.icon_right), contentDescription = null, colorFilter = ColorFilter.tint(Color.Black), modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(rotation)
            )
            Spacer(Modifier.weight(1f))
            if (isSelected) {
                Image(
                    painter = painterResource(R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(16.dp)
                )
            }
        }

        // 二级列表（带动画）
        AnimatedVisibility(
            visible = isExpanded && category.children.isNotEmpty(), enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column {
                category.children.forEach { sub ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubClick(sub) }
                        .padding(start = 64.dp, top = 16.dp, bottom = 16.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sub.name,
                            fontSize = 16.sp,
                            color = Color.Black,
                        )
                        Spacer(Modifier.weight(1f))
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

@Composable fun ClosetTypeBottomSheet(
    categories: List<Category>, currentCategory: SelectedCategory?, visible: () -> Boolean, onDismiss: () -> Unit, onConfirm: (SelectedCategory) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        // 记录每一个一级分类是否展开二级分类
        var expandedMap by remember {
            mutableStateOf(categories.associate { it.id to (it.id == currentCategory?.categoryId) }.toMutableMap())
        }

        // 记录当前选中的分类，包括一二级
        var selectedCategory by remember { mutableStateOf(currentCategory) }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SheetTitleWidget(title = "分类") {
                selectedCategory?.let {
                    onConfirm(it)
                    onDismiss()
                }
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 10.dp)) {
                items(categories, key = { it.id }) { category ->
                    ExpandableCategoryItem(category = category, isExpanded = expandedMap[category.id] == true, isSelected = selectedCategory?.categoryId == category.id, onToggleExpand = {
                        expandedMap = expandedMap.toMutableMap().apply {
                            // 如果存在则取反，不存在则设为true展开
                            put(category.id, !getOrDefault(category.id, false))
                        }
                    }, onSubClick = { sub ->
                        selectedCategory = SelectedCategory(
                            categoryId = category.id, categoryName = category.name, subCategoryId = sub.id, subCategoryName = sub.name
                        )
                    }, isSelectedSub = { sub -> sub.id == selectedCategory?.subCategoryId })
                }
            }
        }
    }
}

// 使用真实数据的组件

@Composable fun CategoryBottomSheet(
    categories: List<CategoryWithSubCategories>,
    categoryEntity: CategoryEntity?,
    subCategoryEntity: SubCategoryEntity?,
    visible: () -> Boolean,
    onDismiss: () -> Unit,
    onSettingClick: (() -> Unit)? = null,
    onConfirm: (CategoryEntity?, SubCategoryEntity?) -> Unit
) {
    CustomBottomSheet(visible = visible(), onDismiss = onDismiss) {
        // 记录每一个一级分类是否展开二级分类
        var expandedMap by remember {
            mutableStateOf(categories.associate { it.category.id to (it.category.id == subCategoryEntity?.categoryId) }.toMutableMap())
        }

        // 记录当前选中的分类，包括一二级
        var selectedCategory by remember { mutableStateOf(categoryEntity) }
        var selectedSubCategory by remember { mutableStateOf(subCategoryEntity) }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SheetTitleWidget(title = "分类", onSettingClick = onSettingClick) {
                if (selectedCategory != null && selectedSubCategory == null && categories.find { it.category.id == selectedCategory?.id && it.subCategories.isNotEmpty() } != null) {
                    Toaster.show("请选择子分类")
                }else {
                    selectedCategory?.let {
                        onConfirm(selectedCategory, selectedSubCategory)
                        onDismiss()
                    }
                }
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 10.dp)) {
                items(categories, key = { it.category.id }) { category ->
                    ExpandableCategoryItem2(category = category, isExpanded = expandedMap[category.category.id] == true, isSelected = selectedCategory?.id == category.category.id, onToggleExpand = {
                        if (it.id != selectedCategory?.id) {
                            selectedCategory = category.category
                            selectedSubCategory = null
                        }
                        expandedMap = expandedMap.toMutableMap().apply {
                            // 如果存在则取反，不存在则设为true展开
                            put(category.category.id, !getOrDefault(category.category.id, false))
                        }
                    }, onSubClick = { sub ->
                        selectedSubCategory = sub
                    }, isSelectedSub = { sub -> sub.id == selectedSubCategory?.id })
                }
            }
        }
    }
}


@Composable fun ExpandableCategoryItem2(
    category: CategoryWithSubCategories,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggleExpand: (CategoryEntity) -> Unit,
    onSubClick: (SubCategoryEntity) -> Unit,
    isSelectedSub: (SubCategoryEntity) -> Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f, animationSpec = tween(durationMillis = 200, easing = LinearEasing), label = "expandArrowRotation"
    )

    Column(modifier = modifier) {
        // 一级分类项
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand(category.category) }
            .padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.category.name,
                fontSize = 16.sp,
                color = Color.Black,
            )
            if (category.subCategories.isNotEmpty()) {
                Image(
                    painter = painterResource(R.mipmap.icon_right), contentDescription = null, colorFilter = ColorFilter.tint(Color.Black), modifier = Modifier
                        .padding(start = 8.dp)
                        .rotate(rotation)
                )
            }

            Spacer(Modifier.weight(1f))
            if (isSelected) {
                Image(
                    painter = painterResource(R.mipmap.icon_selected), contentDescription = null, modifier = Modifier.size(16.dp)
                )
            }
        }

        // 二级列表（带动画）
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
                        Text(
                            text = sub.name,
                            fontSize = 16.sp,
                            color = Color.Black,
                        )
                        Spacer(Modifier.weight(1f))
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