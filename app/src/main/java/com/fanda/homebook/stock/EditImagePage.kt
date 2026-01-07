package com.fanda.homebook.stock

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.fanda.homebook.R
import com.fanda.homebook.components.TopIconAppBar
import com.fanda.homebook.tools.LogUtils
import com.fanda.homebook.ui.theme.HomeBookTheme
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable fun EditImagePage(modifier: Modifier = Modifier, navController: NavController) {

    val context = LocalContext.current

    val imageUri = remember {
        navController.previousBackStackEntry?.savedStateHandle?.get<Uri>("selectedImageUri")
    }

    var curImageUri by remember {
        mutableStateOf(imageUri)
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
                    curImageUri = uri
                    LogUtils.d("裁剪后的结果：${curImageUri}")
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
        curImageUri?.let { sourceUri ->
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

    Scaffold(modifier = modifier.statusBarsPadding(), topBar = {
        TopIconAppBar(
            title = "编辑图片",
            onBackClick = {
                navController.navigateUp()
            },
            rightText = "完成",
            onRightActionClick = {
                // 返回结果
                curImageUri?.let { uri ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "editedImageUri", uri
                    )
                    navController.popBackStack()
                }
            },
            backIconPainter = painterResource(R.mipmap.icon_back),
        )


    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 图片预览区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Gray)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                curImageUri?.let { uri ->
                    AsyncImage(model = uri, contentDescription = "编辑的图片", modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f))
                }
            }

            ControlPanel(onCropClick = {
                val options = UCrop.Options().apply {
//                    setActiveControlsWidgetColor(Color.Black.toArgb())
                    setCompressionQuality(90)
                    setFreeStyleCropEnabled(true)
                    setShowCropGrid(true)
                    setShowCropFrame(true)
                    setToolbarTitle("自由裁剪")
                }
                launchUCrop(options)
            }, onRotateClick = {

            })
        }
    }
}

@Composable fun ControlPanel(
    modifier: Modifier = Modifier, onCropClick: () -> Unit,
    onRotateClick: () -> Unit,
) {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth() // 宽度与父容器相同
                .height(1.dp) // 高度设置为边框宽度
                .background(Color.White) // 背景颜色即为边框颜色
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(colorResource(id = R.color.color_E3EBF5)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clickable {
                    onRotateClick()
                }) {
                Image(painter = painterResource(id = R.mipmap.icon_ratate), contentDescription = null)
                Text(text = "旋转", fontSize = 12.sp, color = colorResource(id = R.color.color_333333))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clickable {
                    onCropClick()
                }) {
                Image(painter = painterResource(id = R.mipmap.icon_crop), contentDescription = null)
                Text(text = "裁剪", fontSize = 12.sp, color = colorResource(id = R.color.color_333333))
            }
        }
    }

}

@Composable @Preview(showBackground = true) fun EditImagePagePreview() {
    HomeBookTheme {
        EditImagePage(modifier = Modifier, navController = rememberNavController())
    }
}
