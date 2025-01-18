package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import java.util.*

abstract class NmsNBTTagCompoundWrapper : NmsWrapper {
    abstract fun getString(key: String, def: String = ""): String

    abstract fun getStringOrNull(key: String): String?

    abstract fun setString(key: String, value: String)

    abstract fun getBoolean(key: String, def: Boolean = false): Boolean

    abstract fun getBooleanOrNull(key: String): Boolean?

    abstract fun setBoolean(key: String, value: Boolean)

    abstract fun getUUID(key: String): UUID

    abstract fun getUUIDOrNull(key: String): UUID?

    abstract fun setUUID(key: String, value: UUID)

    abstract fun getByte(key: String, def: Byte = 0): Byte

    abstract fun getByteOrNull(key: String): Byte?

    abstract fun setByte(key: String, value: Byte)

    abstract fun getShort(key: String, def: Short = 0): Short

    abstract fun getShortOrNull(key: String): Short?

    abstract fun setShort(key: String, value: Short)

    abstract fun getInt(key: String, def: Int = 0): Int

    abstract fun getIntOrNull(key: String): Int?

    abstract fun setInt(key: String, value: Int)

    abstract fun getLong(key: String, def: Long = 0L): Long

    abstract fun getLongOrNull(key: String): Long?

    abstract fun setLong(key: String, value: Long)

    abstract fun getFloat(key: String, def: Float = 0f): Float

    abstract fun getFloatOrNull(key: String): Float?

    abstract fun setFloat(key: String, value: Float)

    abstract fun getDouble(key: String, def: Double = 0.0): Double

    abstract fun getDoubleOrNull(key: String): Double?

    abstract fun setDouble(key: String, value: Double)

    abstract fun getByteArray(key: String, def: ByteArray = byteArrayOf()): ByteArray

    abstract fun getByteArrayOrNull(key: String): ByteArray?

    abstract fun setByteArray(key: String, value: ByteArray)

    abstract fun getIntArray(key: String, def: IntArray = intArrayOf()): IntArray

    abstract fun getIntArrayOrNull(key: String): IntArray?

    abstract fun setIntArray(key: String, value: IntArray)

    abstract fun getLongArray(key: String, def: LongArray = longArrayOf()): LongArray

    abstract fun getLongArrayOrNull(key: String): LongArray?

    abstract fun setLongArray(key: String, value: LongArray)

    abstract fun hasKey(key: String): Boolean

    abstract fun remove(key: String)

    abstract fun isEmpty(): Boolean
}