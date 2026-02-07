package com.fanda.homebook.closet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetCategoryGridWidget
import com.fanda.homebook.closet.viewmodel.CategoryClosetViewModel
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils

/**
 * 衣橱分类页面
 *
 * 显示特定一级分类下的所有二级分类的网格视图
 */
@OptIn(ExperimentalMaterial3Api::class) @Composable fun ClosetCategoryPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: CategoryClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 从ViewModel收集状态
    val uiState by closetViewModel.uiState.collectAsState()
    val groupedClosets by closetViewModel.groupedClosets.collectAsState()

    LogUtils.i("closets", groupedClosets)

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = uiState.categoryEntity.name, // 显示一级分类名称
                onBackClick = {
                    navController.navigateUp()
                }, rightIconPainter = painterResource(R.mipmap.icon_add_grady),      // 添加按钮
                rightNextIconPainter = painterResource(R.mipmap.icon_setting),    // 设置按钮
                onRightActionClick = {
                    // 添加衣橱物品（打开图片选择）
                    closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                }, onRightNextActionClick = {
                    // 跳转到二级分类管理页面
                    navController.navigate("${RoutePath.EditSubCategory.route}?categoryId=${uiState.categoryEntity.id}")
                })
        }) { padding ->
        // 二级分类网格视图
        ClosetCategoryGridWidget(
            data = groupedClosets, modifier = Modifier.padding(padding)
        ) { item ->
            // 网格项点击：跳转到该二级分类的详细列表页面
            navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=-1&subCategoryId=${item.category.id}&categoryName=${item.category.name}&moveToTrash=false")
        }
    }

    // 图片选择弹窗
    SelectPhotoBottomSheet(
        visible = uiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) { selectedUri ->
        closetViewModel.dismissBottomSheet()
        // 跳转到添加衣橱页面，传递选择的图片路径
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${selectedUri}")
    }
}

/**
 * 预览函数，用于在Android Studio中预览衣橱分类页面
 */
@Composable @Preview(showBackground = true) fun ClosetCategoryPagePreview() {
    ClosetCategoryPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}