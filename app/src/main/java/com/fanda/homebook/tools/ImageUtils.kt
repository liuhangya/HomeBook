package com.fanda.homebook.tools

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest



/**
 * 将 Uri 规范化并生成唯一哈希 ID（用于文件名）
 */
fun Uri.toCacheFileId(): String {
    // 规范化：只保留 scheme + authority + path（移除 query, fragment 等易变部分）
    val normalized = this.buildUpon()
        .clearQuery()
        .fragment(null)
        .build()
        .toString()

    // 计算 MD5 哈希（32位小写 hex）
    return normalized.md5()
}

private fun String.md5(): String {
    return MessageDigest.getInstance("MD5")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
}

/**
 * 根据原始 Uri 获取其在 file 目录 中对应的 File（不保证存在）
 */
fun getCacheFileFromUri(context: Context, uri: Uri): File {
    val fileId = uri.toCacheFileId()
    val extension = getExtensionFromUri(context, uri) ?: "jpg"
    return File(context.getExternalFilesDir("image"), "$fileId.$extension")
}

/**
 * （可选）尝试从 Uri 推测文件扩展名
 */
private fun getExtensionFromUri(context: Context, uri: Uri): String? {
    var extension: String? = null
    try {
        // 方法1：从 MIME 类型推测
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            extension = when {
                mimeType.startsWith("image/jpeg") -> "jpg"
                mimeType.startsWith("image/png") -> "png"
                mimeType.startsWith("image/webp") -> "webp"
                mimeType.startsWith("image/gif") -> "gif"
                else -> mimeType.substringAfterLast("/", "bin")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return extension.takeIf { it != "bin" }
}

suspend fun saveUriToFilesDir(context: Context, uri: Uri): File? {
    return withContext(Dispatchers.IO) {
        val cacheFile = getCacheFileFromUri(context, uri)

        // 如果已存在，可选择跳过或覆盖
        if (cacheFile.exists()) {
            LogUtils.i("文件已存在：${cacheFile.absolutePath}")
            return@withContext cacheFile // 已缓存，直接返回
        }

        try {
            LogUtils.i("开始保存文件：$uri , ${cacheFile.absolutePath}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * 删除 context.cacheDir 下的所有文件和子目录（递归删除）
 * @return true 表示删除成功或目录为空，false 表示操作失败
 */
fun clearAllCache(context: Context): Boolean {
    return deleteRecursively(context.cacheDir)
}

/**
 * 递归删除文件或目录
 */
private fun deleteRecursively(file: File): Boolean {
    return if (file.isDirectory) {
        file.listFiles()?.forEach { deleteRecursively(it) }
        file.delete()
    } else {
        file.delete()
    }
}
