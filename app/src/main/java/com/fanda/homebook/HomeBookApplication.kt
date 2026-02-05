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

/**
 * 应用程序主类，继承自Android Application类
 * 负责应用级别的初始化配置和全局状态管理
 */
class HomeBookApplication : Application() {

    /**
     * 应用程序容器，提供对数据仓库和业务组件的全局访问
     * 延迟初始化，在onCreate中赋值
     */
    lateinit var appContainer: AppContainer

    /**
     * 应用级别的协程作用域
     * 使用SupervisorJob，确保子协程的失败不会影响其他子协程
     * 用于执行应用初始化等后台任务
     */
    private val applicationScope = CoroutineScope(SupervisorJob())

    /**
     * 应用创建时的回调方法，执行全局初始化
     */
    override fun onCreate() {
        super.onCreate()
        // 初始化日志库
        initLoggerLib(this)
        // 初始化应用容器（依赖注入容器）
        appContainer = AppContainerImpl(this)
        // 初始化数据库默认数据
        initDefaultDataEntity()
        // 初始化Toast工具
        initToaster()
    }

    /**
     * 初始化默认的数据库实体数据
     * 确保应用首次运行时数据库中有必要的预设数据
     * 在协程中异步执行，避免阻塞主线程
     */
    private fun initDefaultDataEntity() {
        applicationScope.launch {
            // 初始化季节数据
            appContainer.seasonRepository.initializeDatabase()
            // 初始化颜色类型数据
            appContainer.colorTypeRepository.initializeDatabase()
            // 初始化产品数据
            appContainer.productRepository.initializeDatabase()
            // 初始化尺寸数据
            appContainer.sizeRepository.initializeDatabase()
            // 初始化所有者数据
            appContainer.ownerRepository.initializeDatabase()
            // 初始化分类数据
            appContainer.categoryRepository.initializeDatabase()
            // 初始化货架数据
            appContainer.rackRepository.initializeDatabase()
            // 初始化期间数据（如会计期间等）
            appContainer.periodRepository.initializeDatabase()
            // 初始化交易数据
            appContainer.transactionRepository.initializeDatabase()
            // 初始化支付方式数据
            appContainer.payWayRepository.initializeDatabase()
            // 初始化账本数据
            appContainer.bookRepository.initializeDatabase()

            // 注意：这里按顺序初始化，确保有依赖关系的实体正确建立
            // 例如：交易可能依赖于分类、支付方式等
        }
    }

    /**
     * 初始化Toast显示库
     * 配置Toast的显示位置和样式
     */
    private fun initToaster() {
        // 初始化Toaster库
        Toaster.init(this)
        // 设置Toast显示在底部，距离底部300像素
        Toaster.setGravity(Gravity.BOTTOM, 0, 300)
    }

    /**
     * 初始化日志库配置
     *
     * @param application 应用上下文
     */
    private fun initLoggerLib(application: Application) {
        LogUtils.initConfig(application).apply {
            // 是否启用日志
            logSwitch = true
            // 全局日志标签
            globalTag = "嘟嘟"
            // 是否显示日志头信息（如线程、方法等）
            logHeadSwitch = false
            // 是否保存日志到文件
            log2FileSwitch = true
            // 日志文件保存目录
            dir = getLogSavePath(application)
            // 日志文件前缀
            filePrefix = "main_log"
            // 是否显示日志边框
            logBorderSwitch = true
            // 日志文件保留天数
            saveDays = 7
            // 控制台输出的日志级别过滤（Debug及以上）
            consoleFilter = LogUtils.D
            // 文件输出的日志级别过滤（Info及以上）
            fileFilter = LogUtils.I
        }
    }

    /**
     * 获取日志文件保存路径
     * 优先使用外部存储，如果不可用则使用缓存目录
     *
     * @param application 应用上下文
     * @return 日志文件保存的绝对路径
     */
    private fun getLogSavePath(application: Application): String {
        return application.getExternalFilesDir("log")?.absolutePath
            ?: application.cacheDir.absolutePath
    }
}