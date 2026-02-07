package com.fanda.homebook.closet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.common.sheet.SelectPhotoBottomSheet
import com.fanda.homebook.closet.ui.ClosetInfoScreen
import com.fanda.homebook.closet.viewmodel.AddClosetViewModel
import com.fanda.homebook.components.GradientRoundedBoxWithStroke
import com.fanda.homebook.components.ItemOptionMenu
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.data.AppViewModelProvider
import com.fanda.homebook.data.owner.OwnerEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.common.entity.ShowBottomSheetType
import com.fanda.homebook.common.sheet.CategoryExpandBottomSheet
import com.fanda.homebook.common.sheet.ColorTypeBottomSheet
import com.fanda.homebook.common.sheet.GridBottomSheet
import com.fanda.homebook.common.sheet.ListBottomSheet
import com.fanda.homebook.quick.ui.CustomDatePickerModal
import com.fanda.homebook.route.RoutePath
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.tools.convertMillisToDate
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.hjq.toast.Toaster

/**
 * 添加衣橱页面
 *
 * 用于添加新的衣橱物品，包含图片选择和各种属性设置
 */
@Composable fun AddClosetPage(
    modifier: Modifier = Modifier, navController: NavController, addClosetViewModel: AddClosetViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    // 从ViewModel收集状态
    val addClosetUiState by addClosetViewModel.addClosetUiState.collectAsState()
    val colorTypes by addClosetViewModel.colorTypes.collectAsState()
    val products by addClosetViewModel.products.collectAsState()
    val sizes by addClosetViewModel.sizes.collectAsState()
    val categories by addClosetViewModel.categories.collectAsState()

    val colorType by addClosetViewModel.colorType.collectAsState()
    val selectSeasons by addClosetViewModel.selectSeasons.collectAsState()
    val product by addClosetViewModel.product.collectAsState()
    val size by addClosetViewModel.size.collectAsState()
    val owner by addClosetViewModel.owner.collectAsState()
    val category by addClosetViewModel.category.collectAsState()
    val subCategory by addClosetViewModel.subCategory.collectAsState()

    LogUtils.d("category: $category")
    LogUtils.d("subCategory: $subCategory")
    LogUtils.d("AddClosetPage: addClosetUiState: $addClosetUiState")

    // 焦点管理器（用于关闭软键盘）
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.statusBarsPadding(), topBar = {
            TopIconAppBar(
                title = "单品信息",
                onBackClick = {
                    navController.navigateUp()
                },
                rightText = "保存",
                onRightActionClick = {
                    focusManager.clearFocus()
                    // 先校验衣橱的必填参数
                    if (addClosetViewModel.checkParams()) {
                        // 如果选择了同步到账单，需要校验账单相关参数
                        if (addClosetUiState.closetEntity.syncBook) {
                            if (addClosetViewModel.checkBookParams()) {
                                // 先插入账单数据
                                addClosetViewModel.saveQuickEntityDatabase()
                            } else {
                                // 账单参数校验失败，直接返回
                                return@TopIconAppBar
                            }
                        }
                        // 保存衣橱数据到数据库
                        addClosetViewModel.saveClosetEntityDatabase(context) {
                            Toaster.show("保存成功")
                            navController.navigateUp()
                        }
                    }
                },
                backIconPainter = painterResource(R.mipmap.icon_back),
            )
        }) { padding ->
        // 全屏点击区域（用于关闭软键盘）
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() }, onDoubleTap = { focusManager.clearFocus() }, onLongPress = { focusManager.clearFocus() })
            }
            .background(Color.Transparent)) {
            // 主要内容区域（可滚动）
            Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding() // 处理输入法遮挡
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    // 衣橱图片
                    AsyncImage(
                        contentScale = ContentScale.Crop, model = addClosetUiState.imageUri, contentDescription = null, modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // 1:1比例
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                // 点击图片可重新选择
                                addClosetViewModel.updateSheetType(ShowBottomSheetType.SELECT_IMAGE)
                            })

                    Spacer(modifier = Modifier.height(20.dp))

                    // 归属信息
                    GradientRoundedBoxWithStroke {
                        ItemOptionMenu(
                            title = "归属", rightText = owner?.name ?: "", showText = true, modifier = Modifier
                                .height(64.dp)
                                .padding(start = 20.dp, end = 20.dp)
                        ) {
                            focusManager.clearFocus()
                            addClosetViewModel.updateSheetType(ShowBottomSheetType.OWNER)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 衣橱详细信息表单
                    ClosetInfoScreen(
                        bottomComment = addClosetUiState.closetEntity.comment,
                        closetCategory = category?.name ?: "",
                        closetSubCategory = subCategory?.name ?: "",
                        product = product?.name ?: "",
                        color = colorType?.color ?: -1,
                        season = addClosetViewModel.getSeasonDes(selectSeasons),
                        date = convertMillisToDate(
                            addClosetUiState.closetEntity.date, "yyyy-MM-dd"
                        ),
                        syncBook = addClosetUiState.closetEntity.syncBook,
                        size = size?.name ?: "",
                        price = addClosetUiState.closetEntity.price,
                        onCheckedChange = {
                            addClosetViewModel.updateClosetSyncBook(it)
                            LogUtils.d("同步至当日账单： $it")
                        },
                        onBottomCommentChange = {
                            addClosetViewModel.updateClosetComment(it)
                        },
                        onPriceChange = {
                            addClosetViewModel.updateClosetPrice(it)
                        },
                        onClick = {
                            addClosetViewModel.updateSheetType(it)
                        })
                }
            }
        }
    }

    // 各种底部弹窗组件（根据状态显示）

    // 购买日期选择器
    if (addClosetViewModel.showBottomSheet(ShowBottomSheetType.BUY_DATE)) {
        CustomDatePickerModal(initialDate = addClosetUiState.closetEntity.date, onDateSelected = {
            addClosetViewModel.updateClosetDate(it ?: System.currentTimeMillis())
        }, onDismiss = {
            addClosetViewModel.dismissBottomSheet()
        })
    }

    // 品牌选择弹窗
    ListBottomSheet<ProductEntity>(
        initial = product,
        title = "品牌",
        dataSource = products,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.PRODUCT) },
        displayText = { it.name },
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditProduct.route)
        }) {
        addClosetViewModel.updateClosetProduct(it)
    }

    // 归属选择弹窗
    ListBottomSheet<OwnerEntity>(
        initial = owner,
        title = "归属",
        dataSource = addClosetViewModel.owners,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.OWNER) },
        displayText = { it.name },
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) {
        addClosetViewModel.updateClosetOwner(it)
    }

    // 尺码选择弹窗（网格布局）
    GridBottomSheet<SizeEntity>(
        initial = size,
        title = "尺码",
        dataSource = sizes,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SIZE) },
        displayText = { it.name },
        dpSize = DpSize(52.dp, 36.dp),
        column = GridCells.Fixed(5),
        onDismiss = { addClosetViewModel.dismissBottomSheet() },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditSize.route)
        }) {
        addClosetViewModel.updateClosetSize(it)
    }

    // 季节选择弹窗（网格布局）
    GridBottomSheet<SeasonEntity>(
        initial = selectSeasons,
        title = "季节",
        dataSource = addClosetViewModel.seasons,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.SEASON) },
        displayText = { it.name },
        dpSize = DpSize(66.dp, 36.dp),
        column = GridCells.Fixed(4),
        onDismiss = { addClosetViewModel.dismissBottomSheet() }) {
        addClosetViewModel.updateClosetSeason(it)
    }

    // 颜色选择弹窗
    ColorTypeBottomSheet(color = colorType, colorList = colorTypes, visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.COLOR) }, onDismiss = {
        addClosetViewModel.dismissBottomSheet()
    }, onConfirm = {
        addClosetViewModel.updateClosetColor(it)
    }, onSettingClick = {
        addClosetViewModel.dismissBottomSheet()
        navController.navigate(RoutePath.EditColor.route)
    })

    // 分类选择弹窗（可展开）
    CategoryExpandBottomSheet(
        categories = categories,
        categoryEntity = category,
        subCategoryEntity = subCategory,
        visible = { addClosetViewModel.showBottomSheet(ShowBottomSheetType.CATEGORY) },
        onDismiss = {
            addClosetViewModel.dismissBottomSheet()
        },
        onSettingClick = {
            addClosetViewModel.dismissBottomSheet()
            navController.navigate(RoutePath.EditCategory.route)
        },
        onConfirm = { category, subCategory ->
            LogUtils.i("选中的分类： $category, $subCategory")
            addClosetViewModel.updateSelectedCategory(category, subCategory)
        })

    // 图片选择弹窗
    SelectPhotoBottomSheet(
        visible = addClosetViewModel.showBottomSheet(ShowBottomSheetType.SELECT_IMAGE), onDismiss = {
            addClosetViewModel.dismissBottomSheet()
        }) { selectedUri ->
        addClosetViewModel.dismissBottomSheet()
        addClosetViewModel.updateImageUrl(selectedUri)
    }
}

/**
 * 预览函数，用于在Android Studio中预览添加衣橱页面
 */
@Composable @Preview(showBackground = true) fun AddClosetPagePreview() {
    HomeBookTheme {
        AddClosetPage(
            modifier = Modifier.fillMaxWidth(), navController = rememberNavController()
        )
    }
}