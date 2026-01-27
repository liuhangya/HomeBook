package com.fanda.homebook.tools

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatYearMonth(year: Int, month: Int): String = if (month == 0) {
    "${year}年全年"
} else {
    "${year}年${month}月"
}


// ✅ 校验函数：只允许 "123", "12.34", ".5", "0.1" 等格式
fun isValidDecimalInput(text: String): Boolean {
    if (text.isEmpty()) return true

    // 特殊处理：如果整个字符串都是0（包括可能有小数点）
    // 例如: "0", "00", "000", "0.0", "00.00", ".0" 等都应该被允许
    if (text.all { it == '0' || it == '.' } && text.any { it.isDigit() }) {
        // 检查是否有多个小数点
        return text.count { it == '.' } <= 1
    }

    // 不允许以多个 0 开头（如 "01"、"001"），但允许 "0." 或 "0.1"
    if (text.length > 1 && text[0] == '0' && text[1] != '.' && text[1] != ',') {
        return false
    }

    // 只允许数字和一个小数点
    val dotCount = text.count { it == '.' }
    if (dotCount > 1) return false

    // 检查每个字符是否为数字或小数点
    return text.all { it.isDigit() || it == '.' }
}

const val DATE_FORMAT_YMD = "yyyy-MM-dd"


fun convertMillisToDate(millis: Long, format: String = "MM月-dd日"): String {
    if (millis <= 0) return ""
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(Date(millis))
}


/**
 * 根据过期时间显示剩余时间文本
 */
fun formatExpireTime(
    expireTimestamp: Long,
): String {
    // 计算剩余时间（秒）
    val remainingSeconds = (expireTimestamp - System.currentTimeMillis()) / 1000

    return when {
        // 已过期
        remainingSeconds <= 0 -> "已过期"

        // 剩余天数计算
        else -> {
            val remainingDays = TimeUnit.SECONDS.toDays(remainingSeconds)
            when {
                remainingDays >= 365 * 2 -> "剩余2年以上"
                remainingDays >= 365 -> "剩余1年以上"
                else -> "剩余${remainingDays}天"
            }
        }
    }
}

fun formatExpireTimeDetailed(
    expireTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()
): String {
    if (expireTimestamp <= currentTimestamp) {
        return "已过期"
    }

    // 转换为 LocalDateTime 进行更精确的计算
    val expireDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(expireTimestamp / 1000), ZoneId.systemDefault()
    )
    val currentDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(currentTimestamp / 1000), ZoneId.systemDefault()
    )

    // 计算剩余天数
    val remainingDays = ChronoUnit.DAYS.between(currentDateTime, expireDateTime)

    return when {
        remainingDays >= 365 * 2 -> "剩余2年以上"
        remainingDays >= 365 -> "剩余1年以上"
        remainingDays > 0 -> "剩余${remainingDays}天"
        else -> {
            // 剩余不足1天，按小时显示
            val remainingHours = ChronoUnit.HOURS.between(currentDateTime, expireDateTime)
            LogUtils.d("当前时间戳： ${System.currentTimeMillis()}")
            if (remainingHours > 0) {
                "剩余${remainingHours}小时"
            } else {
                "剩余${ChronoUnit.MINUTES.between(currentDateTime, expireDateTime)}分钟"
            }
        }
    }
}

/**
 * 将 DatePicker 返回的 UTC 0 点时间戳转换为本地时区的 0 点时间戳
 */
fun convertToLocalMidnight(utcMillis: Long): Long {
    // 方法1：使用 Java Time API（推荐，API 26+）
    return try {
        // 将 UTC 时间戳转换为 LocalDate
        val utcInstant = java.time.Instant.ofEpochMilli(utcMillis)
        val localDate = utcInstant.atZone(ZoneOffset.UTC)
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDate()

        // 将 LocalDate 转换为本地时区的 0 点时间戳
        localDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: Exception) {
        // 方法2：使用 Calendar（兼容旧版本）
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = utcMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }
}
