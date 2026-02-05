package com.fanda.homebook.tools

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Uri扩展函数 - 将Uri转换为唯一的缓存文件ID
 *
 * 作用：为任意Uri生成一个唯一的、稳定的文件名标识，避免重复下载相同文件
 *
 * 原理：
 * 1. 规范化Uri：移除query参数和fragment等易变部分，只保留核心路径
 * 2. 计算MD5哈希：将规范化后的字符串转换为32位小写十六进制哈希值
 *
 * @return 32位小写MD5哈希字符串，用作文件名唯一标识
 */
fun Uri.toCacheFileId(): String {
    // 规范化Uri：移除query参数和fragment，确保相同文件的Uri能生成相同的ID
    val normalized = this.buildUpon()
        .clearQuery()      // 移除查询参数（如?width=100&height=100）
        .fragment(null)    // 移除片段标识（如#section1）
        .build()
        .toString()

    // 计算规范化Uri的MD5哈希值
    return normalized.md5()
}

/**
 * 字符串扩展函数 - 计算MD5哈希值
 *
 * @return 32位小写十六进制MD5字符串
 */
private fun String.md5(): String {
    return MessageDigest.getInstance("MD5")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }  // 格式化为两位十六进制，小写
}

/**
 * 根据Uri获取对应的缓存文件对象（文件可能不存在）
 *
 * 文件存储位置：context.getExternalFilesDir("image") 目录
 * 文件名格式：{md5哈希值}.{扩展名}
 *
 * @param context 上下文对象，用于获取存储目录
 * @param uri 原始Uri
 * @return 对应的File对象（文件可能尚未创建）
 */
fun getCacheFileFromUri(context: Context, uri: Uri): File {
    // 生成唯一的文件ID（基于Uri的MD5哈希）
    val fileId = uri.toCacheFileId()

    // 推测文件扩展名，默认使用jpg
    val extension = getExtensionFromUri(context, uri) ?: "jpg"

    // 获取外部存储的image目录，构建完整文件路径
    return File(context.getExternalFilesDir("image"), "$fileId.$extension")
}

/**
 * 根据Uri推测文件扩展名
 *
 * 推测策略（按优先级）：
 * 1. 从ContentResolver获取MIME类型
 * 2. 根据MIME类型映射到对应扩展名
 *
 * @param context 上下文对象
 * @param uri 文件Uri
 * @return 文件扩展名（不带点），无法推测时返回null
 */
private fun getExtensionFromUri(context: Context, uri: Uri): String? {
    var extension: String? = null
    try {
        // 通过ContentResolver获取文件的MIME类型
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            // 根据MIME类型映射到文件扩展名
            extension = when {
                mimeType.startsWith("image/jpeg") -> "jpg"   // JPEG图像
                mimeType.startsWith("image/png") -> "png"    // PNG图像
                mimeType.startsWith("image/webp") -> "webp"  // WebP图像
                mimeType.startsWith("image/gif") -> "gif"    // GIF图像
                // 其他类型取最后一个"/"后的部分，如果是未知类型则返回"bin"
                else -> mimeType.substringAfterLast("/", "bin")
            }
        }
    } catch (e: Exception) {
        // 获取MIME类型失败，记录异常但不影响主流程
        e.printStackTrace()
    }

    // 如果推测出的扩展名是"bin"（表示未知类型），则返回null使用默认扩展名
    return extension.takeIf { it != "bin" }
}

/**
 * 将Uri指向的文件保存到应用的files目录中（协程版）
 *
 * 功能：
 * 1. 检查文件是否已缓存，避免重复下载
 * 2. 将Uri指向的文件内容复制到缓存文件中
 * 3. 在IO线程执行，避免阻塞UI线程
 *
 * @param context 上下文对象
 * @param uri 要保存的文件Uri（可以是content://、file://等）
 * @return 保存后的File对象，失败时返回null
 */
suspend fun saveUriToFilesDir(context: Context, uri: Uri): File? {
    // 切换到IO线程执行文件操作
    return withContext(Dispatchers.IO) {
        // 获取Uri对应的缓存文件对象
        val cacheFile = getCacheFileFromUri(context, uri)

        // 检查文件是否已存在（缓存命中）
        if (cacheFile.exists()) {
            LogUtils.i("文件已存在，直接使用缓存：${cacheFile.absolutePath}")
            return@withContext cacheFile  // 已缓存，直接返回现有文件
        }

        // 文件不存在，需要创建并保存
        try {
            LogUtils.i("开始保存文件：Uri=$uri, 目标路径=${cacheFile.absolutePath}")

            // 通过ContentResolver打开输入流
            context.contentResolver.openInputStream(uri)?.use { input ->
                // 创建输出流，将输入流内容复制到缓存文件
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)  // 复制文件内容
                }
            }

            // 保存成功，返回文件对象
            cacheFile
        } catch (e: Exception) {
            // 文件保存失败，记录异常
            e.printStackTrace()
            null  // 返回null表示失败
        }
    }
}

/**
 * 递归删除文件或目录
 *
 * 功能：删除指定文件，如果指定的是目录，则递归删除目录下的所有内容
 *
 * @param file 要删除的文件或目录
 * @return 是否成功删除
 */
private fun deleteRecursively(file: File): Boolean {
    return if (file.isDirectory) {
        // 如果是目录：先递归删除所有子文件和子目录
        file.listFiles()?.forEach { deleteRecursively(it) }
        // 最后删除空目录
        file.delete()
    } else {
        // 如果是文件：直接删除
        file.delete()
    }
}

/**
 * 使用建议和注意事项：
 *
 * 1. 缓存策略：
 *    - 基于MD5的哈希命名确保相同Uri的文件只保存一次
 *    - 规范化Uri避免因query参数不同导致重复缓存
 *
 * 2. 存储位置：
 *    - 使用getExternalFilesDir()，文件随应用卸载自动清理
 *    - 不需要存储权限，在Android 10+上也能正常访问
 *
 * 3. 扩展名处理：
 *    - 优先使用MIME类型推测扩展名
 *    - 未知类型使用"bin"标记，避免覆盖有效文件
 *    - 可以扩展mimeType映射表支持更多文件类型
 *
 * 4. 错误处理：
 *    - 保存失败时返回null，调用方需处理
 *    - 已存在文件直接复用，减少IO操作
 *
 * 5. 性能考虑：
 *    - 在IO线程执行，避免阻塞UI
 *    - MD5计算轻量，适合文件名生成
 *
 * 6. 清理策略：
 *    - 可以考虑定期清理旧文件
 *    - 添加文件大小限制
 *    - 提供手动清理接口
 */