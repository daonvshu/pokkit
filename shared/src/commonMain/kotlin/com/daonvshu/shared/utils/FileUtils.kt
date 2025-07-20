package com.daonvshu.shared.utils

fun String.toValidSystemName(): String {
    return this.replace("[\\\\/:?\"*<>|]+".toRegex(), "")
}