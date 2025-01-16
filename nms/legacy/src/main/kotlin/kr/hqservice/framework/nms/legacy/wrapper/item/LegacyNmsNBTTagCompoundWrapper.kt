package kr.hqservice.framework.nms.legacy.wrapper.item

import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import java.util.*

class LegacyNmsNBTTagCompoundWrapper(
    private val nbtTag: Any,
    private val reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsNBTTagCompoundWrapper {
    companion object {
        private var functions: LegacyNBTTagFunctions? = null

        private fun getFunctions(reflectionWrapper: LegacyNmsReflectionWrapper): LegacyNBTTagFunctions {
            if (functions == null) {
                functions = LegacyNBTTagFunctions(reflectionWrapper)
            }
            return functions!!
        }
    }

    override fun getString(key: String, def: String): String {
        return (getFunctions(reflectionWrapper).getStringFunction.call(nbtTag, key) as? String)?.ifEmpty { def }?: def
    }

    override fun getStringOrNull(key: String): String? {
        return if (hasKey(key)) return getString(key) else null
    }

    override fun setString(key: String, value: String) {
        getFunctions(reflectionWrapper).setStringFunction.call(nbtTag, key, value)
    }

    override fun getBoolean(key: String, def: Boolean): Boolean {
        return getFunctions(reflectionWrapper).getBooleanFunction.call(nbtTag, key) as? Boolean ?: def
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return if (hasKey(key)) return getBoolean(key) else null
    }

    override fun setBoolean(key: String, value: Boolean) {
        getFunctions(reflectionWrapper).setBooleanFunction.call(nbtTag, key, value)
    }

    override fun getUUID(key: String): UUID {
        return getFunctions(reflectionWrapper).getUUIDFunction.call(nbtTag, key) as UUID
    }

    override fun getUUIDOrNull(key: String): UUID? {
        return if (hasKey(key)) return getUUID(key) else null
    }

    override fun setUUID(key: String, value: UUID) {
        getFunctions(reflectionWrapper).setUUIDFunction.call(nbtTag, key, value)
    }

    override fun getByte(key: String, def: Byte): Byte {
        return getFunctions(reflectionWrapper).getByteFunction.call(nbtTag, key) as? Byte ?: def
    }

    override fun getByteOrNull(key: String): Byte? {
        return if (hasKey(key)) getByte(key) else null
    }

    override fun setByte(key: String, value: Byte) {
        getFunctions(reflectionWrapper).setByteFunction.call(nbtTag, key, value)
    }

    override fun getShort(key: String, def: Short): Short {
        return getFunctions(reflectionWrapper).getShortFunction.call(nbtTag, key) as? Short ?: def
    }

    override fun getShortOrNull(key: String): Short? {
        return if (hasKey(key)) getShort(key) else null
    }

    override fun setShort(key: String, value: Short) {
        getFunctions(reflectionWrapper).setShortFunction.call(nbtTag, key, value)
    }

    override fun getInt(key: String, def: Int): Int {
        return getFunctions(reflectionWrapper).getIntFunction.call(nbtTag, key) as? Int ?: def
    }

    override fun getIntOrNull(key: String): Int? {
        return if (hasKey(key)) getInt(key) else null
    }

    override fun setInt(key: String, value: Int) {
        getFunctions(reflectionWrapper).setIntFunction.call(nbtTag, key, value)
    }

    override fun getLong(key: String, def: Long): Long {
        return getFunctions(reflectionWrapper).getLongFunction.call(nbtTag, key) as? Long ?: def
    }

    override fun getLongOrNull(key: String): Long? {
        return if (hasKey(key)) getLong(key) else null
    }

    override fun setLong(key: String, value: Long) {
        getFunctions(reflectionWrapper).setLongFunction.call(nbtTag, key, value)
    }

    override fun getFloat(key: String, def: Float): Float {
        return getFunctions(reflectionWrapper).getFloatFunction.call(nbtTag, key) as? Float ?: def
    }

    override fun getFloatOrNull(key: String): Float? {
        return if (hasKey(key)) getFloat(key) else null
    }

    override fun setFloat(key: String, value: Float) {
        getFunctions(reflectionWrapper).setFloatFunction.call(nbtTag, key, value)
    }

    override fun getDouble(key: String, def: Double): Double {
        return getFunctions(reflectionWrapper).getDoubleFunction.call(nbtTag, key) as? Double ?: def
    }

    override fun getDoubleOrNull(key: String): Double? {
        return if (hasKey(key)) getDouble(key) else null
    }

    override fun setDouble(key: String, value: Double) {
        getFunctions(reflectionWrapper).setDoubleFunction.call(nbtTag, key, value)
    }

    override fun getByteArray(key: String, def: ByteArray): ByteArray {
        return getFunctions(reflectionWrapper).getByteArrayFunction.call(nbtTag, key) as? ByteArray ?: def
    }

    override fun getByteArrayOrNull(key: String): ByteArray? {
        return if (hasKey(key)) getByteArray(key) else null
    }

    override fun setByteArray(key: String, value: ByteArray) {
        getFunctions(reflectionWrapper).setByteArrayFunction.call(nbtTag, key, value)
    }

    override fun getIntArray(key: String, def: IntArray): IntArray {
        return getFunctions(reflectionWrapper).getIntArrayFunction.call(nbtTag, key) as? IntArray ?: def
    }

    override fun getIntArrayOrNull(key: String): IntArray? {
        return if (hasKey(key)) getIntArray(key) else null
    }

    override fun setIntArray(key: String, value: IntArray) {
        getFunctions(reflectionWrapper).setIntArrayFunction.call(nbtTag, key, value)
    }

    override fun getLongArray(key: String, def: LongArray): LongArray {
        return getFunctions(reflectionWrapper).getLongArrayFunction.call(nbtTag, key) as? LongArray ?: def
    }

    override fun getLongArrayOrNull(key: String): LongArray? {
        return if (hasKey(key)) getLongArray(key) else null
    }

    override fun setLongArray(key: String, value: LongArray) {
        getFunctions(reflectionWrapper).setLongArrayFunction.call(nbtTag, key, value)
    }

    override fun hasKey(key: String): Boolean {
        return getFunctions(reflectionWrapper).containsFunction.call(nbtTag, key) as? Boolean ?: false
    }

    override fun remove(key: String) {
        getFunctions(reflectionWrapper).removeFunction.call(nbtTag, key)
    }

    override fun isEmpty(): Boolean {
        return getFunctions(reflectionWrapper).isEmptyFunction.call(nbtTag) as? Boolean ?: true
    }

    override fun getUnwrappedInstance(): Any {
        return nbtTag
    }
}