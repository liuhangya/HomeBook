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

@Retention(AnnotationRetention.SOURCE)
@IntDef(LogUtils.V, LogUtils.D, LogUtils.I, LogUtils.W, LogUtils.E, LogUtils.A)
annotation class LogLevel

object LogUtils {

    const val V = Log.VERBOSE
    const val D = Log.DEBUG
    const val I = Log.INFO
    const val W = Log.WARN
    const val E = Log.ERROR
    const val A = Log.ASSERT

    private const val FILE = 0x10
    private const val JSON = 0x20
    private const val XML = 0x30

    private const val FILE_SEP = "/"
    private const val LINE_SEP = "\n"
    private const val MAX_LEN = 1100
    private const val NOTHING = "log nothing"
    private const val NULL = "null"
    private const val ARGS = "args"

    private const val TOP_BORDER = "┌────────────────────────────────────────────────────────────────────────────────────────"
    private const val MIDDLE_BORDER = "├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private const val BOTTOM_BORDER = "└────────────────────────────────────────────────────────────────────────────────────────"
    private const val LEFT_BORDER = "│ "

    private var config: Config? = null
    private var simpleDateFormat: SimpleDateFormat? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val formatterMap = ArrayMap<Class<*>, IFormatter<*>>()

    // ========================
    // 初始化
    // ========================

    fun initConfig(application: Application): Config {
        config = Config(application)
        return config!!
    }

    // ========================
    // 快捷日志方法（无 tag）
    // ========================

    @JvmStatic fun v(vararg contents: Any?) = log(V, "", *contents)
    @JvmStatic fun d(vararg contents: Any?) = log(D, "", *contents)
    @JvmStatic fun i(vararg contents: Any?) = log(I, "", *contents)
    @JvmStatic fun w(vararg contents: Any?) = log(W, "", *contents)
    @JvmStatic fun e(vararg contents: Any?) = log(E, "", *contents)
    @JvmStatic fun a(vararg contents: Any?) = log(A, "", *contents)

    // ========================
    // 带自定义 tag 的日志
    // ========================

    @JvmStatic fun vTag(tag: String, vararg contents: Any?) = log(V, tag, *contents)
    @JvmStatic fun dTag(tag: String, vararg contents: Any?) = log(D, tag, *contents)
    @JvmStatic fun iTag(tag: String, vararg contents: Any?) = log(I, tag, *contents)
    @JvmStatic fun wTag(tag: String, vararg contents: Any?) = log(W, tag, *contents)
    @JvmStatic fun eTag(tag: String, vararg contents: Any?) = log(E, tag, *contents)
    @JvmStatic fun aTag(tag: String, vararg contents: Any?) = log(A, tag, *contents)

    // ========================
    // 文件 / JSON / XML 日志
    // ========================

    @JvmStatic fun file(content: Any?) = log(FILE or D, config?.globalTag ?: "", content)
    @JvmStatic fun file(@LogLevel type: Int, content: Any?) = log(FILE or type, config?.globalTag ?: "", content)
    @JvmStatic fun file(tag: String, content: Any?) = log(FILE or D, tag, content)
    @JvmStatic fun file(@LogLevel type: Int, tag: String, content: Any?) = log(FILE or type, tag, content)

    @JvmStatic fun json(content: Any?) = log(JSON or D, config?.globalTag ?: "", content)
    @JvmStatic fun json(@LogLevel type: Int, content: Any?) = log(JSON or type, config?.globalTag ?: "", content)
    @JvmStatic fun json(tag: String, content: Any?) = log(JSON or D, tag, content)
    @JvmStatic fun json(@LogLevel type: Int, tag: String, content: Any?) = log(JSON or type, tag, content)

    @JvmStatic fun xml(content: String) = log(XML or D, config?.globalTag ?: "", content)
    @JvmStatic fun xml(@LogLevel type: Int, content: String) = log(XML or type, config?.globalTag ?: "", content)
    @JvmStatic fun xml(tag: String, content: String) = log(XML or D, tag, content)
    @JvmStatic fun xml(@LogLevel type: Int, tag: String, content: String) = log(XML or type, tag, content)

    // ========================
    // 主日志入口
    // ========================

    private fun log(type: Int, tag: String, vararg contents: Any?) {
        val cfg = config ?: return
        if (!cfg.isLogSwitch()) return

        val typeLow = type and 0x0f
        val typeHigh = type and 0xf0

        if (!cfg.log2ConsoleSwitch && !cfg.log2FileSwitch && typeHigh != FILE) return
        if (typeLow < cfg.consoleFilter && typeLow < cfg.fileFilter) return

        val tagHead = processTagAndHead(tag, cfg)
        val body = processBody(typeHigh, *contents)

        if (cfg.log2ConsoleSwitch && typeHigh != FILE && typeLow >= cfg.consoleFilter) {
            print2Console(typeLow, tagHead.tag, tagHead.consoleHead, body)
        }

        if ((cfg.log2FileSwitch || typeHigh == FILE) && typeLow >= cfg.fileFilter) {
            executor.execute {
                print2File(null, typeLow, tagHead.tag, tagHead.fileHead + body, cfg)
            }
        }
    }

    // ========================
    // 辅助方法（简化版）
    // ========================

    private fun processTagAndHead(tag: String, cfg: Config): TagHead {
        val globalTag = cfg.globalTag
        val mTagIsSpace = cfg.mTagIsSpace
        val logHeadSwitch = cfg.logHeadSwitch
        val stackOffset = cfg.stackOffset
        val stackDeep = cfg.stackDeep

        // 如果不需要头部，且 mTagIsSpace 为 false，则直接使用 [globalTag]
//        if (!mTagIsSpace && !logHeadSwitch) {
//            val finalTag = "[$globalTag]"
//            return TagHead(finalTag, null, ": ")
//        }

        // 获取调用栈：new Throwable().getStackTrace()
        val stackTrace = Throwable().stackTrace
        val stackIndex = 3 + stackOffset

        // 边界检查：如果越界，退回到 index=3
        val useSafeIndex = stackIndex >= stackTrace.size
        val targetElement = if (useSafeIndex) stackTrace[3] else stackTrace[stackIndex]
        val fileName = getFileName(targetElement)

        // 构建 TAG —— 四种情况
        val finalTag = when {
            mTagIsSpace && isSpace(tag) -> {
                // 使用文件名（去除 .java / .kt 后缀）
                val index = fileName.indexOf('.')
                val fileNameTag = if (index == -1) fileName else fileName.substring(0, index)
                "[$fileNameTag]"
            }
            mTagIsSpace && !isSpace(tag) -> {
                "[$tag]"
            }
            !mTagIsSpace && isSpace(tag) -> {
                val index = fileName.indexOf('.')
                val fileNameTag = if (index == -1) fileName else fileName.substring(0, index)
                "[$globalTag][$fileNameTag]"
            }
            else -> {
                "[$globalTag][$tag]"
            }
        }

        // 如果不打印 head，直接返回
        if (!logHeadSwitch) {
            return TagHead(finalTag, null, ": ")
        }

        // 构建 head 信息
        val tName = Thread.currentThread().name
        val className = targetElement.className
        val methodName = targetElement.methodName
        val lineNumber = targetElement.lineNumber

        val head = "$tName, $className.$methodName($fileName:$lineNumber)"
        val fileHead = " [$head]: "

        if (stackDeep <= 1 || useSafeIndex) {
            return TagHead(finalTag, arrayOf(head), fileHead)
        }

        // 多层栈（stackDeep > 1）
        val maxDepth = minOf(stackDeep, stackTrace.size - stackIndex)
        val consoleHead = arrayOfNulls<String>(maxDepth)
        consoleHead[0] = head

        // 对齐空格：长度 = tName.length + 2（对应 ", " 的长度）
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

    // 辅助函数：提取文件名（兼容 ProGuard）
    private fun getFileName(element: StackTraceElement): String {
        var fileName = element.fileName
        if (fileName == null) {
            // 某些混淆或内联情况下可能为 null
            fileName = element.className.substringAfterLast('.').ifEmpty { "Unknown" }
        }
        return fileName
    }


    private fun processBody(typeHigh: Int, vararg contents: Any?): String {
        if (contents.isEmpty()) return NULL
        return if (contents.size == 1) {
            formatObject(typeHigh, contents[0])
        } else {
            buildString {
                contents.forEachIndexed { i, obj ->
                    append("$ARGS[$i] = ${formatObject(obj)}$LINE_SEP")
                }
            }.ifEmpty { NOTHING }
        }
    }

    private fun formatObject(type: Int, obj: Any?): String {
        if (obj == null) return NULL
        return when (type) {
            JSON -> LogFormatter.object2String(obj, JSON)
            XML -> LogFormatter.object2String(obj, XML)
            else -> LogFormatter.object2String(obj)
        }
    }

    private fun formatObject(obj: Any?): String {
        if (obj == null) return NULL
        formatterMap[obj.javaClass]?.let {
            @Suppress("UNCHECKED_CAST")
            return (it as IFormatter<Any>).format(obj)
        }
        return LogFormatter.object2String(obj)
    }

    private fun print2Console(type: Int, tag: String, head: Array<out String>?, msg: String) {
        val cfg = config ?: return
        if (cfg.logBorderSwitch) printlnToLog(type, tag, TOP_BORDER)
        head?.forEach { h ->
            printlnToLog(type, tag, if (cfg.logBorderSwitch) "$LEFT_BORDER$h" else h)
        }
        if (head != null && cfg.logBorderSwitch) printlnToLog(type, tag, MIDDLE_BORDER)
        msg.chunked(MAX_LEN).forEach { chunk ->
            if (cfg.logBorderSwitch) {
                chunk.split(LINE_SEP).forEach { line ->
                    printlnToLog(type, tag, "$LEFT_BORDER$line")
                }
            } else {
                printlnToLog(type, tag, chunk)
            }
        }
        if (cfg.logBorderSwitch) printlnToLog(type, tag, BOTTOM_BORDER)
    }

    private fun printlnToLog(type: Int, tag: String, msg: String) {
        Log.println(type, tag, msg)
        config?.onConsoleOutputListener?.onConsoleOutput(type, tag, msg)
    }

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
        val dateStr = fullTime.substring(0, 10)
        val time = fullTime.substring(11)

        val dir = (cfg.dir ?: cfg.defaultDir).let { if (it.endsWith(FILE_SEP)) it else "$it$FILE_SEP" }
        val logDir = File("$dir$dateStr")
        logDir.mkdirs()

        val prefix = cfg.filePrefix.ifEmpty { "util" }
        val ext = cfg.fileExtension.ifEmpty { ".txt" }.let { if (it.startsWith(".")) it else ".$it" }
        val filePath = if (!fileName.isNullOrBlank()) {
            "$dir$dateStr${FILE_SEP}$dateStr _$fileName"
        } else {
            "$dir$dateStr${FILE_SEP}$dateStr _$prefix$ext"
        }

        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            // 可在此处写入设备信息头（参考 Java 的 printDeviceInfo）
        }

        val levelChar = "VDIWEA"[type - V]
        val content = "$time$levelChar/$tag$msg$LINE_SEP"
        cfg.fileWriter?.write(filePath, content) ?: run {
            FileWriter(file, true).use { it.write(content) }
        }
        cfg.onFileOutputListener?.onFileOutput(filePath, content)
    }

    // ========================
    // 工具函数
    // ========================

    fun isSpace(s: String?): Boolean {
        if (s == null) return true
        for (c in s) {
            if (!c.isWhitespace()) return false
        }
        return true
    }

    // ========================
    // 内部类
    // ========================

    private data class TagHead(
        val tag: String,
        val consoleHead: Array<String>?,
        val fileHead: String
    )

    interface IFormatter<T> {
        fun format(t: T): String
    }

    interface IFileWriter {
        fun write(file: String, content: String)
    }

    interface OnConsoleOutputListener {
        fun onConsoleOutput(@LogLevel type: Int, tag: String, content: String)
    }

    interface OnFileOutputListener {
        fun onFileOutput(filePath: String, content: String)
    }

    // ========================
    // 配置类（使用 Kotlin 属性委托简化）
    // ========================

    class Config internal constructor(private val application: Application) {

        var logSwitch: Boolean = true
        var log2ConsoleSwitch: Boolean = true
        var globalTag: String = ""
        var logHeadSwitch: Boolean = true
        var log2FileSwitch: Boolean = false
        var logBorderSwitch: Boolean = true
        var consoleFilter: Int = V
        var fileFilter: Int = V
        var stackDeep: Int = 1
        var stackOffset: Int = 0
        var saveDays: Int = -1
        var dir: String? = null
        var filePrefix: String = "util"
        var fileExtension: String = ".txt"
        var fileWriter: IFileWriter? = null
        var mTagIsSpace: Boolean = false
        var onConsoleOutputListener: OnConsoleOutputListener? = null
        var onFileOutputListener: OnFileOutputListener? = null

        val defaultDir: String by lazy {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
                application.getExternalFilesDir(null) != null
            ) {
                "${application.getExternalFilesDir(null)}$FILE_SEP log$FILE_SEP"
            } else {
                "${application.filesDir}$FILE_SEP log$FILE_SEP"
            }
        }

        fun isLogSwitch(): Boolean {
            // 可集成 adb settings 控制（参考 Java）
            return logSwitch
        }

        // 构建器风格（Kotlin 中通常不需要，但为兼容保留）
        fun setLogSwitch(logSwitch: Boolean) = apply { this.logSwitch = logSwitch }
        fun setConsoleSwitch(consoleSwitch: Boolean) = apply { this.log2ConsoleSwitch = consoleSwitch }
        // ... 其他 setter 同理
    }

    // ========================
    // Formatter（简化示意）
    // ========================

    private object LogFormatter {
        fun object2String(obj: Any?, type: Int = -1): String {
            if (obj == null) return NULL
            return when {
                obj.javaClass.isArray -> array2String(obj)
                obj is Throwable -> Log.getStackTraceString(obj)
                // ... 其他类型处理（Bundle, Intent, JSON, XML）
                else -> obj.toString()
            }
        }

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