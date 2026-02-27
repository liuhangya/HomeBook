package com.fanda.homebook.common.sheet

import android.Manifest
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.fanda.homebook.R
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * 选择照片底部弹窗组件（带裁剪功能）
 *
 * 提供拍照和从相册选择照片的功能，选择后会启动UCrop进行图片裁剪
 *
 * @param modifier 修饰符，用于自定义弹窗内容区域的布局
 * @param visible 控制弹窗的显示/隐藏状态
 * @param onDismiss 弹窗关闭回调函数
 * @param onPhotoSelected 照片选择完成回调，返回裁剪后的Uri
 */
@OptIn(ExperimentalPermissionsApi::class) @Composable fun SelectPhotoBottomSheet(
    modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit, onPhotoSelected: (Uri) -> Unit
) {
    // 获取当前上下文
    val context = LocalContext.current

    // 相机权限状态管理
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // 是否显示权限说明弹窗
    var showPermissionRationale by remember { mutableStateOf(false) }

    // 拍照的临时文件Uri
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // UCrop裁剪配置选项
    val options = UCrop.Options().apply {
        setActiveControlsWidgetColor(Color(0xFF2196F3).toArgb()) // 控件颜色
        setCompressionQuality(90)                                 // 压缩质量
        setFreeStyleCropEnabled(true)                             // 启用自由裁剪
        setShowCropGrid(true)                                     // 显示裁剪网格
        setShowCropFrame(true)                                    // 显示裁剪框
        setToolbarTitle("自由裁剪")                               // 工具栏标题
    }

    // UCrop裁剪结果Launcher
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        LogUtils.d("裁剪结果回调")
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                // 获取裁剪后的Uri
                val outputUri = UCrop.getOutput(result.data!!)
                outputUri?.let { uri ->
                    // 更新当前显示的图片
                    photoUri = uri
                    LogUtils.d("裁剪后的结果：${photoUri}")
                    // 回调裁剪后的Uri给外部
                    photoUri?.let(onPhotoSelected)
                }
            }

            UCrop.RESULT_ERROR -> {
                // 裁剪出错处理
                val error = UCrop.getError(result.data!!)
                error?.printStackTrace()
                LogUtils.e("UCrop error", error)
            }
        }
    }

    /**
     * 启动UCrop图片裁剪
     *
     * @param options UCrop配置选项
     */
    fun launchUCrop(options: UCrop.Options? = null) {
        photoUri?.let { sourceUri ->
            try {
                // 创建目标文件名
                val destinationFileName = "cropped_${System.currentTimeMillis()}.jpg"
                // 创建目标文件Uri
                val destinationUri = Uri.fromFile(
                    File(context.cacheDir, destinationFileName)
                )

                // 配置UCrop
                val uCrop = UCrop.of(sourceUri, destinationUri)

                // 应用自定义选项
                options?.let { uCrop.withOptions(it) }

                // 启动UCrop
                val intent = uCrop.getIntent(context)
                uCropLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 相册选择Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 保存选择的图片Uri
            photoUri = it
            LogUtils.d("Selected from album: $it")
            // 关闭弹窗并启动裁剪
            onDismiss()
            launchUCrop(options)
        }
    }

    // 拍照Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                LogUtils.d("Photo taken: $it")
                // 关闭弹窗并启动裁剪
                onDismiss()
                launchUCrop(options)
            }
        }
    }

    /**
     * 打开相册选择图片
     */
    fun openAlbum() {
        galleryLauncher.launch("image/*")
    }

    /**
     * 打开相机拍照（带权限检查）
     */
    fun openCamera() {
        LogUtils.d("Opening camera...: ${cameraPermissionState.status.isGranted} - ${cameraPermissionState.status.shouldShowRationale}")
        if (cameraPermissionState.status.isGranted) {
            // 已授权，创建临时文件
            val timestamp = System.currentTimeMillis()
            val file = File(context.cacheDir, "photo_$timestamp.jpg")
            // 创建FileProvider Uri（安全方式）
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", // 必须和Manifest中的authorities一致
                file
            )
            photoUri = uri // 保存Uri用于后续回调
            // 启动相机
            cameraLauncher.launch(uri)
        } else {
            if (cameraPermissionState.status.shouldShowRationale) {
                // 用户之前拒绝过权限，显示说明
                showPermissionRationale = true
            } else {
                // 首次请求相机权限
                LogUtils.d("首次请求 camera...: ${cameraPermissionState.status.isGranted}")
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // 权限拒绝说明弹窗
    if (showPermissionRationale) {
        ConfirmDialog(
            title = "请允许访问相机，以便拍摄照片", confirmText = "去授权", onDismissRequest = {
                showPermissionRationale = false
            }) {
            showPermissionRationale = false
            // 再次请求权限
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // 照片选择弹窗主体
    CustomBottomSheet(
        visible = visible, onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中对齐
            verticalArrangement = Arrangement.Center             // 垂直居中对齐
        ) {
            item {
                // 拍照选项
                Text(text = "拍照", fontSize = 16.sp, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        openCamera()
                    }
                    .padding(vertical = 26.dp))

                // 分割线1
                HorizontalDivider(
                    color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                // 相册选项
                Text(text = "相册", fontSize = 16.sp, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        openAlbum()
                    }
                    .padding(vertical = 26.dp))

                // 分割线2
                HorizontalDivider(
                    color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                // 取消按钮
                Box(
                    modifier = Modifier.padding(24.dp)
                ) {
                    SelectableRoundedButton(
                        text = "取消",
                        selected = false,
                        onClick = onDismiss,
                        cornerSize = 27.dp,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        fontSize = 16.sp,
                        interaction = true
                    )
                }
            }
        }
    }
}

/**
 * 预览函数，用于在Android Studio中预览照片选择弹窗
 */
@Composable @Preview(showBackground = true) fun SelectPhotoBottomSheetPreview() {
    HomeBookTheme {
        SelectPhotoBottomSheet(visible = true, onDismiss = {}, onPhotoSelected = {})
    }
}