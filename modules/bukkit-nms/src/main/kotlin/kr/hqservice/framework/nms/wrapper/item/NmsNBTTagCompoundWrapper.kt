package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

class NmsNBTTagCompoundWrapper(
    val persistentDataContainer: PersistentDataContainer
) : NmsWrapper {
    fun getString(key: String, def: String): String {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.STRING) ?: def
    }

    fun getStringOrNull(key: String): String? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.STRING)
    }

    fun setString(key: String, value: String) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.STRING, value)
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.BOOLEAN) ?: def
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.BOOLEAN) 
    }

    fun setBoolean(key: String, value: Boolean) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.BOOLEAN, value)
    }

    fun getUUID(key: String): UUID {
        return getUUIDOrNull(key) ?: throw NullPointerException("uuid is null")
    }

    fun getUUIDOrNull(key: String): UUID? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.STRING)?.run(UUID::fromString)
    }

    fun setUUID(key: String, value: UUID) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.STRING, value.toString())
    }

    fun getByte(key: String, def: Byte): Byte {
        return getByteOrNull(key) ?: def
    }

    fun getByteOrNull(key: String): Byte? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.BYTE)
    }

    fun setByte(key: String, value: Byte) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.BYTE, value)
    }

    fun getShort(key: String, def: Short): Short {
        return getShortOrNull(key) ?: def
    }

    fun getShortOrNull(key: String): Short? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.SHORT)
    }

    fun setShort(key: String, value: Short) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.SHORT, value)
    }

    fun getInt(key: String, def: Int): Int {
        return getIntOrNull(key) ?: def
    }

    fun getIntOrNull(key: String): Int? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.INTEGER)
    }

    fun setInt(key: String, value: Int) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.INTEGER, value)
    }

    fun getLong(key: String, def: Long): Long {
        return getLongOrNull(key) ?: def
    }

    fun getLongOrNull(key: String): Long? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.LONG)
    }

    fun setLong(key: String, value: Long) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.LONG, value)
    }

    fun getFloat(key: String, def: Float): Float {
        return getFloatOrNull(key) ?: def
    }

    fun getFloatOrNull(key: String): Float? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.FLOAT)
    }

    fun setFloat(key: String, value: Float) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.FLOAT, value)
    }

    fun getDouble(key: String, def: Double): Double {
        return getDoubleOrNull(key) ?: def
    }

    fun getDoubleOrNull(key: String): Double? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.DOUBLE)
    }

    fun setDouble(key: String, value: Double) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.DOUBLE, value)
    }

    fun getByteArray(key: String, def: ByteArray): ByteArray {
        return getByteArrayOrNull(key) ?: def
    }

    fun getByteArrayOrNull(key: String): ByteArray? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.BYTE_ARRAY)
    }

    fun setByteArray(key: String, value: ByteArray) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.BYTE_ARRAY, value)
    }

    fun getIntArray(key: String, def: IntArray): IntArray {
        return getIntArrayOrNull(key) ?: def
    }

    fun getIntArrayOrNull(key: String): IntArray? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.INTEGER_ARRAY)
    }

    fun setIntArray(key: String, value: IntArray) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.INTEGER_ARRAY, value)
    }

    fun getLongArray(key: String, def: LongArray): LongArray {
        return getLongArrayOrNull(key) ?: def
    }

    fun getLongArrayOrNull(key: String): LongArray? {
        return persistentDataContainer.get(NamespacedKey("hq_container", key), PersistentDataType.LONG_ARRAY)
    }

    fun setLongArray(key: String, value: LongArray) {
        persistentDataContainer.set(NamespacedKey("hq_container", key), PersistentDataType.LONG_ARRAY, value)
    }

    fun hasKey(key: String): Boolean {
        return persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.STRING)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.BOOLEAN)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.LONG)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.SHORT)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.BYTE)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.INTEGER)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.INTEGER_ARRAY)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.LONG_ARRAY)
                || persistentDataContainer.has(NamespacedKey("hq_container", key), PersistentDataType.BYTE_ARRAY)
    }

    fun remove(key: String) {
        persistentDataContainer.remove(NamespacedKey("hq_container", key))
    }

    fun isEmpty(): Boolean {
        return persistentDataContainer.isEmpty
    }

    override fun getUnwrappedInstance(): Any {
        return persistentDataContainer
    }
}