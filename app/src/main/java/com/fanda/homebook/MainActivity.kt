package com.fanda.homebook

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.fanda.homebook.ui.theme.HomeBookTheme

/**
 * 主活动类，负责应用的启动和 UI 的初始化。
 */
class MainActivity : ComponentActivity() {
    /**
     * 在 Activity 创建时调用，用于初始化界面和设置系统 UI 样式。
     *
     * @param savedInstanceState 保存的实例状态，用于恢复 Activity 状态。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置系统 UI 标志，使内容显示在底部导航栏后面
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 启用边缘到边缘显示，并设置状态栏样式为透明
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(), darkScrim = Color.Transparent.toArgb()
            ),
        )

        // 使用 Compose 设置 Activity 的内容
        setContent {
            HomeBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    HomeBookApp() // 调用主应用组件
                }
            }
        }
    }
}

/**
 * 预览函数，用于在 Android Studio 中预览 Compose UI 组件。
 */
@Preview(showBackground = true) @Composable fun GreetingPreview() {
    HomeBookTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            HomeBookApp() // 预览主应用组件
        }
    }
}
