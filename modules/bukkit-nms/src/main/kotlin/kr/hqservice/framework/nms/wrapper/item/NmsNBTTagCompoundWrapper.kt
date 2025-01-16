package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import java.util.*

interface NmsNBTTagCompoundWrapper : NmsWrapper {
    fun getString(key: String, def: String = ""): String

    fun getStringOrNull(key: String): String?

    fun setString(key: String, value: String)

    fun getBoolean(key: String, def: Boolean = false): Boolean

    fun getBooleanOrNull(key: String): Boolean?

    fun setBoolean(key: String, value: Boolean)

    fun getUUID(key: String): UUID

    fun getUUIDOrNull(key: String): UUID?

    fun setUUID(key: String, value: UUID)

    fun getByte(key: String, def: Byte = 0): Byte

    fun getByteOrNull(key: String): Byte?

    fun setByte(key: String, value: Byte)

    fun getShort(key: String, def: Short = 0): Short

    fun getShortOrNull(key: String): Short?

    fun setShort(key: String, value: Short)

    fun getInt(key: String, def: Int = 0): Int

    fun getIntOrNull(key: String): Int?

    fun setInt(key: String, value: Int)

    fun getLong(key: String, def: Long = 0L): Long

    fun getLongOrNull(key: String): Long?

    fun setLong(key: String, value: Long)

    fun getFloat(key: String, def: Float = 0f): Float

    fun getFloatOrNull(key: String): Float?

    fun setFloat(key: String, value: Float)

    fun getDouble(key: String, def: Double = 0.0): Double

    fun getDoubleOrNull(key: String): Double?

    fun setDouble(key: String, value: Double)

    fun getByteArray(key: String, def: ByteArray = byteArrayOf()): ByteArray

    fun getByteArrayOrNull(key: String): ByteArray?

    fun setByteArray(key: String, value: ByteArray)

    fun getIntArray(key: String, def: IntArray = intArrayOf()): IntArray

    fun getIntArrayOrNull(key: String): IntArray?

    fun setIntArray(key: String, value: IntArray)

    fun getLongArray(key: String, def: LongArray = longArrayOf()): LongArray

    fun getLongArrayOrNull(key: String): LongArray?

    fun setLongArray(key: String, value: LongArray)

    fun hasKey(key: String): Boolean

    fun remove(key: String)

    fun isEmpty(): Boolean
}