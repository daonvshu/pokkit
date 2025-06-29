package com.daonvshu.shared.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(val time: String, val message: String)

object LogCollector {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> get() = _logs

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun addLog(msg: String) {
        val now = timeFormat.format(Date())
        // 每次新增插入到末尾
        _logs.value = (arrayOf(LogEntry(now, msg)) + _logs.value).take(100)
    }

    // 可选：清空日志
    fun clear() {
        _logs.value = emptyList()
    }
}