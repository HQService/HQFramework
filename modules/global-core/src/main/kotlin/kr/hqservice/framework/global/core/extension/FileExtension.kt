package kr.hqservice.framework.global.core.extension

import java.io.File

fun File.mkdirsIfNotExists(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}