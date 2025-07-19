package com.daonvshu.shared.utils

enum class SizeUnit {
    B, KB, MB, GB, TB, PB, EB, ZB, YB
}

fun Long.friendlySize(splitThr: Long = 1024L): String {
    if (this < 0) return "0 B" // 负数返回0B，或可抛异常

    var size = this.toDouble()
    var unitIndex = 0

    while (size >= splitThr && unitIndex < SizeUnit.entries.toTypedArray().lastIndex) {
        size /= splitThr
        unitIndex++
    }

    // 格式化：如果小数部分为0，则不显示小数
    val sizeStr = if (size % 1.0 == 0.0) {
        size.toInt().toString()
    } else {
        String.format("%.2f", size)
    }

    return "$sizeStr ${SizeUnit.entries[unitIndex]}"
}