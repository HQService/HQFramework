package kr.hqservice.framework.nms.v21.wrapper.item

import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.*

class NmsNBTTagCompoundWrapperImpl(
    private val persistentDataContainer: PersistentDataContainer?
) : NmsNBTTagCompoundWrapper() {
    override fun getString(key: String, def: String): String {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.STRING) ?: def
    }

    override fun getStringOrNull(key: String): String? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.STRING)
    }

    override fun setString(key: String, value: String) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.STRING, value)
    }

    override fun getBoolean(key: String, def: Boolean): Boolean {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BOOLEAN) ?: def
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BOOLEAN)
    }

    override fun setBoolean(key: String, value: Boolean) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BOOLEAN, value)
    }

    override fun getUUID(key: String): UUID {
        return getUUIDOrNull(key.toKey()) ?: throw NullPointerException("uuid is null")
    }

    override fun getUUIDOrNull(key: String): UUID? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.STRING)?.run(UUID::fromString)
    }

    override fun setUUID(key: String, value: UUID) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.STRING, value.toString())
    }

    override fun getByte(key: String, def: Byte): Byte {
        return getByteOrNull(key.toKey()) ?: def
    }

    override fun getByteOrNull(key: String): Byte? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BYTE)
    }

    override fun setByte(key: String, value: Byte) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BYTE, value)
    }

    override fun getShort(key: String, def: Short): Short {
        return getShortOrNull(key.toKey()) ?: def
    }

    override fun getShortOrNull(key: String): Short? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.SHORT)
    }

    override fun setShort(key: String, value: Short) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.SHORT, value)
    }

    override fun getInt(key: String, def: Int): Int {
        return getIntOrNull(key.toKey()) ?: def
    }

    override fun getIntOrNull(key: String): Int? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.INTEGER)
    }

    override fun setInt(key: String, value: Int) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.INTEGER, value)
    }

    override fun getLong(key: String, def: Long): Long {
        return getLongOrNull(key.toKey()) ?: def
    }

    override fun getLongOrNull(key: String): Long? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.LONG)
    }

    override fun setLong(key: String, value: Long) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.LONG, value)
    }

    override fun getFloat(key: String, def: Float): Float {
        return getFloatOrNull(key.toKey()) ?: def
    }

    override fun getFloatOrNull(key: String): Float? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.FLOAT)
    }

    override fun setFloat(key: String, value: Float) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.FLOAT, value)
    }

    override fun getDouble(key: String, def: Double): Double {
        return getDoubleOrNull(key.toKey()) ?: def
    }

    override fun getDoubleOrNull(key: String): Double? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.DOUBLE)
    }

    override fun setDouble(key: String, value: Double) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.DOUBLE, value)
    }

    override fun getByteArray(key: String, def: ByteArray): ByteArray {
        return getByteArrayOrNull(key.toKey()) ?: def
    }

    override fun getByteArrayOrNull(key: String): ByteArray? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BYTE_ARRAY)
    }

    override fun setByteArray(key: String, value: ByteArray) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.BYTE_ARRAY, value)
    }

    override fun getIntArray(key: String, def: IntArray): IntArray {
        return getIntArrayOrNull(key.toKey()) ?: def
    }

    override fun getIntArrayOrNull(key: String): IntArray? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.INTEGER_ARRAY)
    }

    override fun setIntArray(key: String, value: IntArray) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.INTEGER_ARRAY, value)
    }

    override fun getLongArray(key: String, def: LongArray): LongArray {
        return getLongArrayOrNull(key.toKey()) ?: def
    }

    override fun getLongArrayOrNull(key: String): LongArray? {
        return persistentDataContainer?.get(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.LONG_ARRAY)
    }

    override fun setLongArray(key: String, value: LongArray) {
        persistentDataContainer?.set(NamespacedKey(key.toKey(), "hq_tag"), PersistentDataType.LONG_ARRAY, value)
    }

    override fun hasKey(key: String): Boolean {
        return persistentDataContainer?.keys?.contains(NamespacedKey(key.toKey(), "hq_tag")) ?: false
    }

    override fun remove(key: String) {
        persistentDataContainer?.remove(NamespacedKey(key.toKey(), "hq_tag"))
    }

    override fun isEmpty(): Boolean {
        return persistentDataContainer?.isEmpty ?: true
    }

    override fun getUnwrappedInstance(): Any {
        return persistentDataContainer ?: 1
    }

    private fun String.toKey(): String = this
        .replace("-", "_")
        .replace(" ", "_")
        .lowercase()
}