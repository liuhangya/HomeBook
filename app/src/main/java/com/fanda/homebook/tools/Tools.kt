package com.fanda.homebook.tools

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



// ✅ 校验函数：只允许 "123", "12.34", ".5", "0.1" 等格式
fun isValidDecimalInput(text: String): Boolean {
    if (text.isEmpty()) return true

    // 不允许以多个 0 开头（如 "00"、"01"），但允许 "0." 或 "0.1"
    if (text.length > 1 && text[0] == '0' && text[1] != '.' && text[1] != ',') {
        return false
    }

    // 只允许数字和一个小数点
    val dotCount = text.count { it == '.' }
    if (dotCount > 1) return false

    // 检查每个字符是否为数字或小数点
    return text.all { it.isDigit() || it == '.' }
}


fun convertMillisToDate(millis: Long , format: String = "MM月-dd日"): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(Date(millis))
}