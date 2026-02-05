package com.fanda.homebook.tools

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt


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
const val DATE_FORMAT_MD = "MM月dd日"
const val DATE_FORMAT_MD_HM = "M月d HH:mm"


fun convertMillisToDate(millis: Long, format: String = "MM月-dd日"): String {
    if (millis <= 0) return ""
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(Date(millis))
}

/**
 * 根据 openDate 和 shelfMonth 计算过期时间戳
 * @param openDate 开封时间戳（毫秒）
 * @param shelfMonth 保质期月数
 * @return 计算出的过期时间戳，如果参数无效返回 -1
 */
fun calculateExpireDateFromOpenDate(openDate: Long, shelfMonth: Int): Long {
    // 参数验证
    if (openDate <= 0 || shelfMonth <= 0) {
        return -1
    }

    try {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = openDate
            add(Calendar.MONTH, shelfMonth)
        }
        return calendar.timeInMillis
    } catch (e: Exception) {
        e.printStackTrace()
        return -1
    }
}


fun formatExpireTimeDetailed(
    expireTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()
): String {
    if (expireTimestamp <= 0) return ""
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
 * 将日期的时间戳加上当前时间的时分秒
 * @param dateMillis 日期的时间戳（0点）
 * @return 加上当前时分秒后的时间戳
 */
fun addCurrentTimeToDate(dateMillis: Long): Long {
    val calendar = Calendar.getInstance()

    // 获取当前时间的时分秒毫秒
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentSecond = calendar.get(Calendar.SECOND)
    val currentMillisecond = calendar.get(Calendar.MILLISECOND)

    // 设置到选中的日期
    calendar.timeInMillis = dateMillis
    calendar.set(Calendar.HOUR_OF_DAY, currentHour)
    calendar.set(Calendar.MINUTE, currentMinute)
    calendar.set(Calendar.SECOND, currentSecond)
    calendar.set(Calendar.MILLISECOND, currentMillisecond)

    return calendar.timeInMillis
}

// 毫秒时间戳 → LocalDate
fun millisToLocalDate(timestampMillis: Long): Pair<Int, Int> {
    val localDate = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()) // 使用系统默认时区
        .toLocalDate()
    return localDate.year to localDate.monthValue
}

fun Float.roundToString(decimalPlaces: Int = 2): String {
    val factor = 10f.pow(decimalPlaces)
    val roundedValue = (this * factor).roundToInt() / factor
    return "%.${decimalPlaces}f".format(roundedValue)
}

