package com.fanda.homebook

import android.app.Application
import android.view.Gravity
import com.fanda.homebook.data.AppContainer
import com.fanda.homebook.data.AppContainerImpl
import com.fanda.homebook.tools.LogUtils
import com.hjq.toast.Toaster
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
        Toaster.init(this)
        Toaster.setGravity(Gravity.BOTTOM, 0, 300)
    }

    /*
    * 初始化默认的数据库实体
    * */
    private fun initDefaultDataEntity() {
        applicationScope.launch {
            appContainer.seasonRepository.initializeDatabase()
            appContainer.colorTypeRepository.initializeDatabase()
            appContainer.productRepository.initializeDatabase()
            appContainer.sizeRepository.initializeDatabase()
            appContainer.ownerRepository.initializeDatabase()
            appContainer.categoryRepository.initializeDatabase()
            appContainer.rackRepository.initializeDatabase()
            appContainer.periodRepository.initializeDatabase()
            appContainer.transactionRepository.initializeDatabase()
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