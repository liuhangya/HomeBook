package com.fanda.homebook

import android.app.Application
import com.fanda.homebook.data.AppContainer
import com.fanda.homebook.data.AppContainerImpl
import com.fanda.homebook.tools.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeBookApplication : Application() {

    lateinit var appContainer: AppContainer

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        initLoggerLib(this)
        appContainer = AppContainerImpl(this)
        initDefaultDataEntity()
    }

    /*
    * 初始化默认的数据库实体
    * */
    private fun initDefaultDataEntity() {
        applicationScope.launch {
            appContainer.seasonRepository.initializeDatabase()
            appContainer.colorTypeRepository.initializeDatabase()
        }
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