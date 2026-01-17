package com.fanda.homebook.closet.sheet

import android.Manifest
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
import com.fanda.homebook.components.CustomBottomSheet
import com.fanda.homebook.R
import com.fanda.homebook.components.ConfirmDialog
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalPermissionsApi::class) @Composable fun SelectPhotoBottomSheet(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit, onPhotoSelected: (Uri) -> Unit) {

    val context = LocalContext.current

    // 只需 CAMERA 权限（用于拍照）
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    // 用于拍照的临时 URI（保存到 app cache）
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // 编辑图片逻辑 ================================================

    val options = UCrop.Options().apply {
        setActiveControlsWidgetColor(Color(0xFF2196F3).toArgb())
        setCompressionQuality(90)
        setFreeStyleCropEnabled(true)
        setShowCropGrid(true)
        setShowCropFrame(true)
        setToolbarTitle("自由裁剪")
    }

    // UCrop 结果处理器
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        LogUtils.d("裁剪结果回调")
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val outputUri = UCrop.getOutput(result.data!!)
                outputUri?.let { uri ->
                    // 更新当前显示的图片
                    photoUri = uri
                    LogUtils.d("裁剪后的结果：${photoUri}")
                    photoUri?.let(onPhotoSelected)
                }
            }

            UCrop.RESULT_ERROR -> {
                val error = UCrop.getError(result.data!!)
                // 处理错误
                error?.printStackTrace()
                LogUtils.e("UCrop error", error)
            }
        }
    }

    // 启动UCrop
    fun launchUCrop(options: UCrop.Options? = null) {
        photoUri?.let { sourceUri ->
            try {
                // 创建目标文件
                val destinationFileName = "cropped_${System.currentTimeMillis()}.jpg"
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


    // 相册选择 Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            LogUtils.d("Selected from album: $it")
            onDismiss()
            launchUCrop(options)
        }
    }

    // 拍照 Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let {
                LogUtils.d("Photo taken: $it")
                onDismiss()
                launchUCrop(options)
            }
        }
    }


    // 封装：打开相册
    fun openAlbum() {
        galleryLauncher.launch("image/*")
    }

    // 封装：打开相机（带权限检查）
    fun openCamera() {
        LogUtils.d("Opening camera...: ${cameraPermissionState.status.isGranted} - ${cameraPermissionState.status.shouldShowRationale}")
        if (cameraPermissionState.status.isGranted) {
            // 创建临时文件 URI（存到 cacheDir，无需权限）
            val timestamp = System.currentTimeMillis()
            val file = File(context.cacheDir, "photo_$timestamp.jpg")
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", // 必须和 Manifest 中 authorities 一致
                file
            )
            photoUri = uri // 保存用于后续回调
            cameraLauncher.launch(uri)
        } else {
            if (cameraPermissionState.status.shouldShowRationale) {
                // 用户之前拒绝过，显示说明
                showPermissionRationale = true
            } else {
                // 首次请求
                LogUtils.d("首次请求 camera...: ${cameraPermissionState.status.isGranted}")
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // 权限拒绝弹窗
    if (showPermissionRationale) {
        ConfirmDialog(title = "请允许访问相机，以便拍摄照片", confirmText = "去授权", onDismissRequest = {
            showPermissionRationale = false
        }) {
            showPermissionRationale = false
            cameraPermissionState.launchPermissionRequest()
        }
    }

    CustomBottomSheet(visible = visible, onDismiss = onDismiss) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "拍照", fontSize = 16.sp, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openCamera()
                }
                .padding(vertical = 26.dp))
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Text(text = "相册", fontSize = 16.sp, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openAlbum()
                }
                .padding(vertical = 26.dp))
            HorizontalDivider(
                color = colorResource(id = R.color.color_E1E9F3), modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Box(modifier = Modifier.padding(24.dp)) {
                SelectableRoundedButton(
                    text = "取消", selected = false, onClick = onDismiss, cornerSize = 27.dp, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp), fontSize = 16.sp
                )
            }
        }
    }


}

@Composable @Preview(showBackground = true) fun SelectPhotoBottomSheetPreview() {
    HomeBookTheme {
        SelectPhotoBottomSheet(visible = true, onDismiss = {}, onPhotoSelected = {})
    }
}