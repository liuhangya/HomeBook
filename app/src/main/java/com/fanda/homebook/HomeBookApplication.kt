package com.fanda.homebook

import android.app.Application
import com.fanda.homebook.tools.LogUtils

class HomeBookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLoggerLib(this)
    }

    private fun initLoggerLib(application: Application) {
        LogUtils.initConfig(application).apply {
            logSwitch = true
            globalTag = "嘟嘟"
            logHeadSwitch = false
            log2FileSwitch = true
            dir = getLogSavePath(application)
            filePrefix = "main_log"
            logBorderSwitch = true
            saveDays = 7
            consoleFilter = LogUtils.D
            fileFilter = LogUtils.I
        }
    }

    private fun getLogSavePath(application: Application) = application.getExternalFilesDir("log")?.absolutePath ?: application.cacheDir.absolutePath
}