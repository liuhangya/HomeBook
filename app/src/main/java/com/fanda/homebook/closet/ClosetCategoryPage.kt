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
import com.fanda.homebook.closet.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetCategoryGridWidget
import com.fanda.homebook.closet.viewmodel.CategoryClosetViewModel
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.entity.ShowBottomSheetType
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils


@OptIn(ExperimentalMaterial3Api::class) @Composable fun ClosetCategoryPage(
    modifier: Modifier = Modifier, navController: NavController, closetViewModel: CategoryClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val uiState by closetViewModel.uiState.collectAsState()
    val groupedClosets by closetViewModel.groupedClosets.collectAsState()

    LogUtils.i("closets", groupedClosets)

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(title = uiState.categoryEntity.name, onBackClick = {
            navController.navigateUp()
        }, rightIconPainter = painterResource(R.mipmap.icon_add_grady), rightNextIconPainter = painterResource(R.mipmap.icon_setting), onRightActionClick = {
            closetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
        }, onRightNextActionClick = {
            // 跳到二级分类管理页面
            navController.navigate("${RoutePath.EditSubCategory.route}?categoryId=${uiState.categoryEntity.id}")
        })
    }) { padding ->
        ClosetCategoryGridWidget(data = groupedClosets, modifier = Modifier.padding(padding)) {
            navController.navigate("${RoutePath.ClosetDetailCategory.route}?categoryId=-1&subCategoryId=${it.category.id}&categoryName=${it.category.name}")
        }
    }

    SelectPhotoBottomSheet(
        visible = uiState.sheetType == ShowBottomSheetType.SELECT_IMAGE, onDismiss = {
            closetViewModel.dismissBottomSheet()
        }) {
        closetViewModel.dismissBottomSheet()
        navController.navigate("${RoutePath.AddCloset.route}?imagePath=${it}")
    }

}


@Composable @Preview(showBackground = true) fun ClosetCategoryPagePreview() {
    ClosetCategoryPage(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(), navController = rememberNavController()
    )
}