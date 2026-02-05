package com.fanda.homebook.tools

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.annotation.IntDef
import androidx.collection.ArrayMap
import java.io.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.contracts.contract

// ========================
// 类型定义 & 常量
// ========================

/**
 * 日志级别注解，限制参数只能使用 LogUtils 中定义的常量
 * 保证类型安全，避免传入非法日志级别
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(LogUtils.V, LogUtils.D, LogUtils.I, LogUtils.W, LogUtils.E, LogUtils.A)
annotation class LogLevel

/**
 * 增强型日志工具类
 * 功能特性：
 * 1. 支持控制台和文件双重输出
 * 2. 支持自定义格式化输出（JSON/XML/普通对象）
 * 3. 支持日志边框、调用栈信息
 * 4. 支持日志文件按日期分割和自动清理
 * 5. 线程安全，支持异步文件写入
 *
 * 设计模式：单例 + 建造者模式（通过Config类配置）
 */
object LogUtils {

    // ========================
    // 日志级别常量（与android.util.Log对齐）
    // ========================
    const val V = Log.VERBOSE  // 详细日志，用于开发调试
    const val D = Log.DEBUG    // 调试信息，用于排查问题
    const val I = Log.INFO     // 普通信息，记录关键流程
    const val W = Log.WARN     // 警告信息，潜在问题
    const val E = Log.ERROR    // 错误信息，需要关注
    const val A = Log.ASSERT   // 断言失败，严重问题

    // ========================
    // 日志类型常量（用于特殊格式输出）
    // ========================
    private const val FILE = 0x10    // 文件专用日志类型
    private const val JSON = 0x20    // JSON格式日志
    private const val XML = 0x30     // XML格式日志

    // ========================
    // 格式化常量
    // ========================
    private const val FILE_SEP = "/"          // 文件路径分隔符
    private const val LINE_SEP = "\n"         // 行分隔符
    private const val MAX_LEN = 1100          // 单条日志最大长度（避免Logcat截断）
    private const val NOTHING = "log nothing" // 空内容占位符
    private const val NULL = "null"           // null值占位符
    private const val ARGS = "args"           // 参数前缀

    // ========================
    // 日志边框常量（美化输出）
    // ========================
    private const val TOP_BORDER = "┌────────────────────────────────────────────────────────────────────────────────────────"
    private const val MIDDLE_BORDER = "├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private const val BOTTOM_BORDER = "└────────────────────────────────────────────────────────────────────────────────────────"
    private const val LEFT_BORDER = "│ "      // 左边框

    // ========================
    // 静态变量
    // ========================
    private var config: Config? = null                // 全局配置实例
    private var simpleDateFormat: SimpleDateFormat? = null  // 日期格式化器（懒加载）
    private val executor = Executors.newSingleThreadExecutor()  // 单线程池，用于异步文件写入
    private val formatterMap = ArrayMap<Class<*>, IFormatter<*>>()  // 自定义格式化器映射表

    // ========================
    // 初始化方法
    // ========================

    /**
     * 初始化日志配置
     *
     * 注意：必须在 Application.onCreate() 中调用
     *
     * @param application 应用上下文
     * @return Config配置对象，用于链式配置
     *
     * 使用示例：
     * LogUtils.initConfig(application)
     *     .logSwitch(true)
     *     .globalTag("MyApp")
     *     .log2FileSwitch(true)
     */
    fun initConfig(application: Application): Config {
        config = Config(application)
        return config!!
    }

    // ========================
    // 快捷日志方法（使用全局tag）
    // ========================

    /**
     * 详细级别日志（使用全局tag）
     */
    @JvmStatic fun v(vararg contents: Any?) = log(V, "", *contents)

    /**
     * 调试级别日志（使用全局tag）
     */
    @JvmStatic fun d(vararg contents: Any?) = log(D, "", *contents)

    /**
     * 信息级别日志（使用全局tag）
     */
    @JvmStatic fun i(vararg contents: Any?) = log(I, "", *contents)

    /**
     * 警告级别日志（使用全局tag）
     */
    @JvmStatic fun w(vararg contents: Any?) = log(W, "", *contents)

    /**
     * 错误级别日志（使用全局tag）
     */
    @JvmStatic fun e(vararg contents: Any?) = log(E, "", *contents)

    /**
     * 断言级别日志（使用全局tag）
     */
    @JvmStatic fun a(vararg contents: Any?) = log(A, "", *contents)

    // ========================
    // 带自定义tag的日志方法
    // ========================

    @JvmStatic fun vTag(tag: String, vararg contents: Any?) = log(V, tag, *contents)
    @JvmStatic fun dTag(tag: String, vararg contents: Any?) = log(D, tag, *contents)
    @JvmStatic fun iTag(tag: String, vararg contents: Any?) = log(I, tag, *contents)
    @JvmStatic fun wTag(tag: String, vararg contents: Any?) = log(W, tag, *contents)
    @JvmStatic fun eTag(tag: String, vararg contents: Any?) = log(E, tag, *contents)
    @JvmStatic fun aTag(tag: String, vararg contents: Any?) = log(A, tag, *contents)

    // ========================
    // 特殊格式日志方法
    // ========================

    /**
     * 文件专用日志方法（自动写入文件，不输出到控制台）
     */
    @JvmStatic fun file(content: Any?) = log(FILE or D, config?.globalTag ?: "", content)
    @JvmStatic fun file(@LogLevel type: Int, content: Any?) = log(FILE or type, config?.globalTag ?: "", content)
    @JvmStatic fun file(tag: String, content: Any?) = log(FILE or D, tag, content)
    @JvmStatic fun file(@LogLevel type: Int, tag: String, content: Any?) = log(FILE or type, tag, content)

    /**
     * JSON格式日志（自动格式化JSON字符串）
     */
    @JvmStatic fun json(content: Any?) = log(JSON or D, config?.globalTag ?: "", content)
    @JvmStatic fun json(@LogLevel type: Int, content: Any?) = log(JSON or type, config?.globalTag ?: "", content)
    @JvmStatic fun json(tag: String, content: Any?) = log(JSON or D, tag, content)
    @JvmStatic fun json(@LogLevel type: Int, tag: String, content: Any?) = log(JSON or type, tag, content)

    /**
     * XML格式日志（自动格式化XML字符串）
     */
    @JvmStatic fun xml(content: String) = log(XML or D, config?.globalTag ?: "", content)
    @JvmStatic fun xml(@LogLevel type: Int, content: String) = log(XML or type, config?.globalTag ?: "", content)
    @JvmStatic fun xml(tag: String, content: String) = log(XML or D, tag, content)
    @JvmStatic fun xml(@LogLevel type: Int, tag: String, content: String) = log(XML or type, tag, content)

    // ========================
    // 核心日志处理逻辑
    // ========================

    /**
     * 主日志处理方法（私有）
     *
     * @param type 日志类型和级别的组合（高4位为类型，低4位为级别）
     * @param tag 日志标签
     * @param contents 日志内容数组
     */
    private fun log(type: Int, tag: String, vararg contents: Any?) {
        val cfg = config ?: return  // 配置未初始化，直接返回
        if (!cfg.isLogSwitch()) return  // 日志开关关闭

        val typeLow = type and 0x0f  // 提取日志级别（低4位）
        val typeHigh = type and 0xf0 // 提取日志类型（高4位）

        // 判断是否需要输出（控制台开关/文件开关）
        if (!cfg.log2ConsoleSwitch && !cfg.log2FileSwitch && typeHigh != FILE) return

        // 根据过滤级别判断是否需要输出
        if (typeLow < cfg.consoleFilter && typeLow < cfg.fileFilter) return

        // 处理标签和头部信息
        val tagHead = processTagAndHead(tag, cfg)
        // 处理日志正文内容
        val body = processBody(typeHigh, *contents)

        // 输出到控制台
        if (cfg.log2ConsoleSwitch && typeHigh != FILE && typeLow >= cfg.consoleFilter) {
            print2Console(typeLow, tagHead.tag, tagHead.consoleHead, body)
        }

        // 输出到文件（异步执行）
        if ((cfg.log2FileSwitch || typeHigh == FILE) && typeLow >= cfg.fileFilter) {
            executor.execute {
                print2File(null, typeLow, tagHead.tag, tagHead.fileHead + body, cfg)
            }
        }
    }

    // ========================
    // 辅助处理方法
    // ========================

    /**
     * 处理标签和头部信息
     *
     * 功能：
     * 1. 根据配置生成合适的日志tag
     * 2. 生成调用栈头部信息（线程、类、方法、行号）
     *
     * @param tag 用户传入的tag
     * @param cfg 配置对象
     * @return TagHead对象，包含处理后的tag和头部信息
     */
    private fun processTagAndHead(tag: String, cfg: Config): TagHead {
        val globalTag = cfg.globalTag
        val mTagIsSpace = cfg.mTagIsSpace
        val logHeadSwitch = cfg.logHeadSwitch
        val stackOffset = cfg.stackOffset
        val stackDeep = cfg.stackDeep

        // 获取调用栈信息
        val stackTrace = Throwable().stackTrace
        val stackIndex = 3 + stackOffset  // 跳过工具类内部调用栈

        // 边界检查，防止数组越界
        val useSafeIndex = stackIndex >= stackTrace.size
        val targetElement = if (useSafeIndex) stackTrace[3] else stackTrace[stackIndex]
        val fileName = getFileName(targetElement)

        // 构建最终tag（四种组合情况）
        val finalTag = when {
            mTagIsSpace && isSpace(tag) -> {
                // 使用文件名作为tag（去除扩展名）
                val index = fileName.indexOf('.')
                val fileNameTag = if (index == -1) fileName else fileName.substring(0, index)
                "[$fileNameTag]"
            }
            mTagIsSpace && !isSpace(tag) -> {
                // 使用传入的tag
                "[$tag]"
            }
            !mTagIsSpace && isSpace(tag) -> {
                // 组合全局tag和文件名
                val index = fileName.indexOf('.')
                val fileNameTag = if (index == -1) fileName else fileName.substring(0, index)
                "[$globalTag][$fileNameTag]"
            }
            else -> {
                // 组合全局tag和传入tag
                "[$globalTag][$tag]"
            }
        }

        // 如果不打印头部信息，直接返回
        if (!logHeadSwitch) {
            return TagHead(finalTag, null, ": ")
        }

        // 构建头部信息
        val tName = Thread.currentThread().name
        val className = targetElement.className
        val methodName = targetElement.methodName
        val lineNumber = targetElement.lineNumber

        val head = "$tName, $className.$methodName($fileName:$lineNumber)"
        val fileHead = " [$head]: "

        // 如果只需要一层调用栈，直接返回
        if (stackDeep <= 1 || useSafeIndex) {
            return TagHead(finalTag, arrayOf(head), fileHead)
        }

        // 构建多层调用栈信息
        val maxDepth = minOf(stackDeep, stackTrace.size - stackIndex)
        val consoleHead = arrayOfNulls<String>(maxDepth)
        consoleHead[0] = head

        // 对齐空格，美化输出
        val spaceLen = tName.length + 2
        val space = " ".repeat(spaceLen)

        for (i in 1 until maxDepth) {
            val element = stackTrace[stackIndex + i]
            val deepClassName = element.className
            val deepMethodName = element.methodName
            val deepFileName = getFileName(element)
            val deepLineNumber = element.lineNumber
            consoleHead[i] = "$space$deepClassName.$deepMethodName($deepFileName:$deepLineNumber)"
        }

        return TagHead(finalTag, consoleHead as Array<String>, fileHead)
    }

    /**
     * 从StackTraceElement中提取文件名
     *
     * 注意：经过ProGuard混淆后，fileName可能为null
     *
     * @param element 调用栈元素
     * @return 文件名
     */
    private fun getFileName(element: StackTraceElement): String {
        var fileName = element.fileName
        if (fileName == null) {
            // 从类名中提取最后一部分作为文件名
            fileName = element.className.substringAfterLast('.').ifEmpty { "Unknown" }
        }
        return fileName
    }

    /**
     * 处理日志正文内容
     *
     * @param typeHigh 日志类型（JSON/XML等）
     * @param contents 日志内容数组
     * @return 格式化后的字符串
     */
    private fun processBody(typeHigh: Int, vararg contents: Any?): String {
        if (contents.isEmpty()) return NULL

        return if (contents.size == 1) {
            // 单个内容，直接格式化
            formatObject(typeHigh, contents[0])
        } else {
            // 多个内容，格式化为参数列表
            buildString {
                contents.forEachIndexed { i, obj ->
                    append("$ARGS[$i] = ${formatObject(obj)}$LINE_SEP")
                }
            }.ifEmpty { NOTHING }
        }
    }

    /**
     * 格式化对象（根据类型）
     */
    private fun formatObject(type: Int, obj: Any?): String {
        if (obj == null) return NULL
        return when (type) {
            JSON -> LogFormatter.object2String(obj, JSON)
            XML -> LogFormatter.object2String(obj, XML)
            else -> LogFormatter.object2String(obj)
        }
    }

    /**
     * 格式化对象（优先使用自定义格式化器）
     */
    private fun formatObject(obj: Any?): String {
        if (obj == null) return NULL
        // 查找自定义格式化器
        formatterMap[obj.javaClass]?.let {
            @Suppress("UNCHECKED_CAST")
            return (it as IFormatter<Any>).format(obj)
        }
        // 使用默认格式化器
        return LogFormatter.object2String(obj)
    }

    /**
     * 输出到控制台
     *
     * @param type 日志级别
     * @param tag 日志标签
     * @param head 头部信息数组
     * @param msg 日志正文
     */
    private fun print2Console(type: Int, tag: String, head: Array<out String>?, msg: String) {
        val cfg = config ?: return

        // 输出边框
        if (cfg.logBorderSwitch) printlnToLog(type, tag, TOP_BORDER)

        // 输出头部信息
        head?.forEach { h ->
            printlnToLog(type, tag, if (cfg.logBorderSwitch) "$LEFT_BORDER$h" else h)
        }

        // 输出分隔线
        if (head != null && cfg.logBorderSwitch) printlnToLog(type, tag, MIDDLE_BORDER)

        // 输出正文（分块处理，避免Logcat截断）
        msg.chunked(MAX_LEN).forEach { chunk ->
            if (cfg.logBorderSwitch) {
                // 带边框模式，每行都添加左边框
                chunk.split(LINE_SEP).forEach { line ->
                    printlnToLog(type, tag, "$LEFT_BORDER$line")
                }
            } else {
                // 普通模式
                printlnToLog(type, tag, chunk)
            }
        }

        // 输出底部边框
        if (cfg.logBorderSwitch) printlnToLog(type, tag, BOTTOM_BORDER)
    }

    /**
     * 输出到Logcat（带监听回调）
     */
    private fun printlnToLog(type: Int, tag: String, msg: String) {
        Log.println(type, tag, msg)
        config?.onConsoleOutputListener?.onConsoleOutput(type, tag, msg)
    }

    /**
     * 输出到文件（异步）
     *
     * 文件名格式：yyyy_MM_dd/yyyy_MM_dd _文件名前缀.txt
     * 内容格式：时间 级别/tag 内容
     *
     * @param fileName 自定义文件名（可选）
     * @param type 日志级别
     * @param tag 日志标签
     * @param msg 日志内容
     * @param cfg 配置对象
     */
    private fun print2File(
        fileName: String?,
        type: Int,
        tag: String,
        msg: String,
        cfg: Config
    ) {
        val date = Date()
        val sdf = simpleDateFormat ?: SimpleDateFormat("yyyy_MM_dd HH:mm:ss.SSS ", Locale.getDefault()).also { simpleDateFormat = it }
        val fullTime = sdf.format(date)
        val dateStr = fullTime.substring(0, 10)  // 提取日期部分（yyyy_MM_dd）
        val time = fullTime.substring(11)        // 提取时间部分（HH:mm:ss.SSS）

        // 构建目录路径
        val dir = (cfg.dir ?: cfg.defaultDir).let { if (it.endsWith(FILE_SEP)) it else "$it$FILE_SEP" }
        val logDir = File("$dir$dateStr")
        logDir.mkdirs()

        // 构建文件路径
        val prefix = cfg.filePrefix.ifEmpty { "util" }
        val ext = cfg.fileExtension.ifEmpty { ".txt" }.let { if (it.startsWith(".")) it else ".$it" }
        val filePath = if (!fileName.isNullOrBlank()) {
            "$dir$dateStr${FILE_SEP}$dateStr _$fileName"
        } else {
            "$dir$dateStr${FILE_SEP}$dateStr _$prefix$ext"
        }

        // 创建文件（如果不存在）
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            // 可在此处写入设备信息头（版本、设备型号等）
        }

        // 构建日志内容
        val levelChar = "VDIWEA"[type - V]  // 将日志级别转换为字符
        val content = "$time$levelChar/$tag$msg$LINE_SEP"

        // 写入文件（支持自定义写入器）
        cfg.fileWriter?.write(filePath, content) ?: run {
            FileWriter(file, true).use { it.write(content) }
        }

        // 触发文件输出监听
        cfg.onFileOutputListener?.onFileOutput(filePath, content)
    }

    // ========================
    // 工具函数
    // ========================

    /**
     * 检查字符串是否为null或仅包含空白字符
     *
     * @param s 待检查字符串
     * @return true: 空或空白; false: 包含非空白字符
     */
    fun isSpace(s: String?): Boolean {
        if (s == null) return true
        for (c in s) {
            if (!c.isWhitespace()) return false
        }
        return true
    }

    // ========================
    // 内部数据类
    // ========================

    /**
     * 标签和头部信息封装类
     *
     * @property tag 处理后的日志标签
     * @property consoleHead 控制台头部信息数组（可空）
     * @property fileHead 文件头部信息字符串
     */
    private data class TagHead(
        val tag: String,
        val consoleHead: Array<String>?,
        val fileHead: String
    )

    // ========================
    // 接口定义
    // ========================

    /**
     * 自定义对象格式化器接口
     *
     * @param T 要格式化的对象类型
     */
    interface IFormatter<T> {
        /**
         * 将对象格式化为字符串
         */
        fun format(t: T): String
    }

    /**
     * 自定义文件写入器接口
     * 可用于实现加密、压缩等特殊写入逻辑
     */
    interface IFileWriter {
        /**
         * 写入文件
         *
         * @param file 文件路径
         * @param content 要写入的内容
         */
        fun write(file: String, content: String)
    }

    /**
     * 控制台输出监听器
     */
    interface OnConsoleOutputListener {
        /**
         * 控制台输出时的回调
         *
         * @param type 日志级别
         * @param tag 日志标签
         * @param content 日志内容
         */
        fun onConsoleOutput(@LogLevel type: Int, tag: String, content: String)
    }

    /**
     * 文件输出监听器
     */
    interface OnFileOutputListener {
        /**
         * 文件输出时的回调
         *
         * @param filePath 文件路径
         * @param content 写入的内容
         */
        fun onFileOutput(filePath: String, content: String)
    }

    // ========================
    // 配置类
    // ========================

    /**
     * 日志配置类（建造者模式）
     *
     * 使用示例：
     * LogUtils.initConfig(app)
     *     .logSwitch(true)
     *     .globalTag("MyApp")
     *     .log2FileSwitch(true)
     *     .dir("/sdcard/MyApp/logs")
     */
    class Config internal constructor(private val application: Application) {

        var logSwitch: Boolean = true                    // 总日志开关
        var log2ConsoleSwitch: Boolean = true            // 控制台输出开关
        var globalTag: String = ""                       // 全局标签
        var logHeadSwitch: Boolean = true                // 是否显示头部信息
        var log2FileSwitch: Boolean = false              // 文件输出开关
        var logBorderSwitch: Boolean = true              // 是否显示边框
        var consoleFilter: Int = V                       // 控制台过滤级别（>=此级别才输出）
        var fileFilter: Int = V                          // 文件过滤级别
        var stackDeep: Int = 1                           // 调用栈深度
        var stackOffset: Int = 0                         // 调用栈偏移量
        var saveDays: Int = -1                           // 日志文件保留天数（-1表示不清理）
        var dir: String? = null                          // 自定义日志目录
        var filePrefix: String = "util"                  // 日志文件名前缀
        var fileExtension: String = ".txt"               // 日志文件扩展名
        var fileWriter: IFileWriter? = null              // 自定义文件写入器
        var mTagIsSpace: Boolean = false                 // 是否使用空格tag
        var onConsoleOutputListener: OnConsoleOutputListener? = null    // 控制台输出监听
        var onFileOutputListener: OnFileOutputListener? = null          // 文件输出监听

        /**
         * 默认日志目录
         * 优先使用外部存储，如果不可用则使用内部存储
         */
        val defaultDir: String by lazy {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                application.getExternalFilesDir(null) != null
            ) {
                "${application.getExternalFilesDir(null)}$FILE_SEP log$FILE_SEP"
            } else {
                "${application.filesDir}$FILE_SEP log$FILE_SEP"
            }
        }

        /**
         * 检查日志开关
         * 可在此集成动态开关（如通过ADB命令控制）
         */
        fun isLogSwitch(): Boolean {
            // 未来可添加：检查ADB设置或远程配置
            return logSwitch
        }

        // ========================
        // 链式配置方法（Kotlin风格）
        // ========================

        /**
         * 设置总日志开关
         */
        fun logSwitch(enable: Boolean) = apply { this.logSwitch = enable }

        /**
         * 设置控制台输出开关
         */
        fun consoleSwitch(enable: Boolean) = apply { this.log2ConsoleSwitch = enable }

        /**
         * 设置全局标签
         */
        fun globalTag(tag: String) = apply { this.globalTag = tag }

        /**
         * 设置头部信息开关
         */
        fun logHeadSwitch(enable: Boolean) = apply { this.logHeadSwitch = enable }

        /**
         * 设置文件输出开关
         */
        fun log2FileSwitch(enable: Boolean) = apply { this.log2FileSwitch = enable }

        /**
         * 设置边框显示开关
         */
        fun logBorderSwitch(enable: Boolean) = apply { this.logBorderSwitch = enable }

        /**
         * 设置控制台过滤级别
         */
        fun consoleFilter(@LogLevel level: Int) = apply { this.consoleFilter = level }

        /**
         * 设置文件过滤级别
         */
        fun fileFilter(@LogLevel level: Int) = apply { this.fileFilter = level }

        /**
         * 设置调用栈深度
         */
        fun stackDeep(depth: Int) = apply { this.stackDeep = depth }

        /**
         * 设置调用栈偏移量
         */
        fun stackOffset(offset: Int) = apply { this.stackOffset = offset }

        /**
         * 设置日志保留天数
         */
        fun saveDays(days: Int) = apply { this.saveDays = days }

        /**
         * 设置自定义日志目录
         */
        fun dir(path: String) = apply { this.dir = path }

        /**
         * 设置日志文件名前缀
         */
        fun filePrefix(prefix: String) = apply { this.filePrefix = prefix }

        /**
         * 设置日志文件扩展名
         */
        fun fileExtension(ext: String) = apply { this.fileExtension = ext }

        /**
         * 设置自定义文件写入器
         */
        fun fileWriter(writer: IFileWriter) = apply { this.fileWriter = writer }

        /**
         * 设置是否使用空格tag
         */
        fun mTagIsSpace(enable: Boolean) = apply { this.mTagIsSpace = enable }

        /**
         * 设置控制台输出监听器
         */
        fun onConsoleOutputListener(listener: OnConsoleOutputListener) = apply { this.onConsoleOutputListener = listener }

        /**
         * 设置文件输出监听器
         */
        fun onFileOutputListener(listener: OnFileOutputListener) = apply { this.onFileOutputListener = listener }
    }

    // ========================
    // 格式化器
    // ========================

    /**
     * 默认对象格式化器
     */
    private object LogFormatter {
        /**
         * 将对象转换为字符串
         *
         * @param obj 要转换的对象
         * @param type 转换类型（JSON/XML等）
         * @return 格式化后的字符串
         */
        fun object2String(obj: Any?, type: Int = -1): String {
            if (obj == null) return NULL
            return when {
                obj.javaClass.isArray -> array2String(obj)  // 数组类型
                obj is Throwable -> Log.getStackTraceString(obj)  // 异常类型
                // 可扩展其他类型处理：Bundle, Intent, JSON, XML等
                else -> obj.toString()  // 默认使用toString()
            }
        }

        /**
         * 将数组转换为字符串
         */
        private fun array2String(obj: Any): String = when (obj) {
            is Array<*> -> Arrays.deepToString(obj)
            is BooleanArray -> obj.contentToString()
            is ByteArray -> obj.contentToString()
            is CharArray -> obj.contentToString()
            is DoubleArray -> obj.contentToString()
            is FloatArray -> obj.contentToString()
            is IntArray -> obj.contentToString()
            is LongArray -> obj.contentToString()
            is ShortArray -> obj.contentToString()
            else -> throw IllegalArgumentException("Unsupported array type: ${obj.javaClass}")
        }
    }
}

/**
 * 使用建议：
 *
 * 1. 初始化配置（Application中）：
 *    LogUtils.initConfig(application)
 *        .logSwitch(BuildConfig.DEBUG)  // 调试模式开启
 *        .globalTag("HomeBook")
 *        .log2FileSwitch(true)          // 生产环境记录文件日志
 *        .saveDays(7)                   // 保留7天日志
 *        .dir(getExternalLogDir())      // 指定日志目录
 *
 * 2. 普通日志记录：
 *    LogUtils.d("用户登录成功", userId)
 *    LogUtils.iTag("Network", "请求完成", url, responseCode)
 *
 * 3. 异常记录：
 *    try {
 *        // 业务代码
 *    } catch (e: Exception) {
 *        LogUtils.e("操作失败", e)
 *    }
 *
 * 4. 文件日志（不输出到控制台）：
 *    LogUtils.file("关键操作记录", operationData)
 *
 * 5. JSON格式日志：
 *    LogUtils.json("API响应", jsonString)
 *
 * 6. 自定义格式化器：
 *    LogUtils.formatterMap[User::class.java] = object : IFormatter<User> {
 *        override fun format(t: User): String = "User{id=${t.id}, name=${t.name}}"
 *    }
 *
 * 注意事项：
 * 1. 避免在循环中频繁记录大对象，影响性能
 * 2. 敏感信息（密码、token等）不要记录到日志
 * 3. 生产环境适当提高过滤级别（如ERROR以上）
 * 4. 定期清理日志文件，避免占用过多存储空间
 */