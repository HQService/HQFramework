package kr.hqservice.framework.global.core.extension

import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

private const val MAX_INFLATED_BYTES = 32 * 1024 * 1024

private class LimitedByteArrayOutputStream : ByteArrayOutputStream() {
    override fun write(b: Int) {
        ensureCapacityFor(1)
        super.write(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        ensureCapacityFor(len)
        super.write(b, off, len)
    }

    private fun ensureCapacityFor(len: Int) {
        if (count.toLong() + len > MAX_INFLATED_BYTES) throw IllegalArgumentException("inflated payload exceeds limit")
    }
}

fun ByteArray.compress(): ByteArray {
    ByteArrayOutputStream().use {
        DeflaterOutputStream(it).use { outputStream ->
            outputStream.write(this)
        }
        return it.toByteArray()
    }
}

fun ByteArray.decompress(): ByteArray {
    LimitedByteArrayOutputStream().use {
        InflaterOutputStream(it).use { inputStream ->
            inputStream.write(this)
        }
        return it.toByteArray()
    }
}