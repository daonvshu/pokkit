package com.daonvshu.shared.utils

import java.io.File

fun String.toValidSystemName(): String {
    return this.replace("[\\\\/:?\"*<>|]+".toRegex(), "")
}

fun String.dir(createBySubName: Boolean, subName: String = ""): File {
    val file = File(this)
    if (createBySubName) {
        val validSubName = subName.toValidSystemName()
        if (file.name == validSubName) {
            return file
        }
        val subDir = File(file, validSubName)
        if (!subDir.exists()) {
            subDir.mkdirs()
        }
        return subDir
    } else {
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }
}