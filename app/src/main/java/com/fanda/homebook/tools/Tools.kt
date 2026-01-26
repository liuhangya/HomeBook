package com.fanda.homebook.tools

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatYearMonth(year: Int, month: Int): String =
    if (month == 0) {
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


fun convertMillisToDate(millis: Long, format: String = "MM月-dd日"): String {
    if (millis <= 0) return ""
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(Date(millis))
}