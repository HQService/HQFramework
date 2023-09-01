package kr.hqservice.framework.global.core.extension

import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

fun ByteArray.compress(): ByteArray {
    ByteArrayOutputStream().use {
        DeflaterOutputStream(it).use { outputStream ->
            outputStream.write(this)
        }
        return it.toByteArray()
    }
}

fun ByteArray.decompress(): ByteArray {
    ByteArrayOutputStream().use {
        InflaterOutputStream(it).use { inputStream ->
            inputStream.write(this)
        }
        return it.toByteArray()
    }
}