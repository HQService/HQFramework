package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import java.util.UUID

class NmsNBTTagCompoundWrapper(
    private val nbtTag: Any,
    reflectionWrapper: NmsReflectionWrapper
) : NmsWrapper {

    private val nbtTagClass = reflectionWrapper.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt"))

    private val getStringFunction = reflectionWrapper.getFunction(nbtTagClass, "getString", listOf(String::class),
        Version.V_17.handleFunction("l") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128461_") { setParameterClasses(String::class) } // ~1.20.2
    )

    private val getBooleanFunction = reflectionWrapper.getFunction(nbtTagClass, "getBoolean", listOf(String::class),
        Version.V_17.handleFunction("q") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128471_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getUUIDFunction = reflectionWrapper.getFunction(nbtTagClass, "getUUID", listOf(String::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128342_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getByteFunction = reflectionWrapper.getFunction(nbtTagClass, "getByte", listOf(String::class),
        Version.V_17.handleFunction("f") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128445_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getShortFunction = reflectionWrapper.getFunction(nbtTagClass, "getShort", listOf(String::class),
        Version.V_17.handleFunction("g") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128448_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getIntFunction = reflectionWrapper.getFunction(nbtTagClass, "getInt", listOf(String::class),
        Version.V_17.handleFunction("h") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128451_") { setParameterClasses(String::class) } // ~1.20.2
    )

    private val getLongFunction = reflectionWrapper.getFunction(nbtTagClass, "getLong", listOf(String::class),
        Version.V_17.handleFunction("i") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128454_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getFloatFunction = reflectionWrapper.getFunction(nbtTagClass, "getFloat", listOf(String::class),
        Version.V_17.handleFunction("j") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128457_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getDoubleFunction = reflectionWrapper.getFunction(nbtTagClass, "getDouble", listOf(String::class),
        Version.V_17.handleFunction("k") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128459_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getByteArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "getByteArray", listOf(String::class),
        Version.V_17.handleFunction("m") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128463_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getIntArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "getIntArray", listOf(String::class),
        Version.V_17.handleFunction("n") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128465_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val getLongArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "getLongArray", listOf(String::class),
        Version.V_17.handleFunction("o") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128467_") { setParameterClasses(String::class) }, // ~1.20.2
    )

    private val setStringFunction = reflectionWrapper.getFunction(nbtTagClass, "putString", listOf(String::class, String::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, String::class) },
        Version.V_17_FORGE.handleFunction("m_128359_") { setParameterClasses(String::class, String::class) } // ~1.20.2
    )

    private val setBooleanFunction = reflectionWrapper.getFunction(nbtTagClass, "putBoolean", listOf(String::class, Boolean::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_128379_") { setParameterClasses(String::class, Boolean::class) } // ~1.20.2
    )

    private val setUUIDFunction = reflectionWrapper.getFunction(nbtTagClass, "putUUID", listOf(String::class, UUID::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, UUID::class) },
        Version.V_17_FORGE.handleFunction("m_128362_") { setParameterClasses(String::class, UUID::class) } // ~1.20.2
    )

    private val setByteFunction = reflectionWrapper.getFunction(nbtTagClass, "putByte", listOf(String::class, Byte::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Byte::class) },
        Version.V_17_FORGE.handleFunction("m_128344_") { setParameterClasses(String::class, Byte::class) } // ~1.20.2
    )

    private val setShortFunction = reflectionWrapper.getFunction(nbtTagClass, "putShort", listOf(String::class, Short::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Short::class) },
        Version.V_17_FORGE.handleFunction("m_128376_") { setParameterClasses(String::class, Short::class) } // ~1.20.2
    )

    private val setIntFunction = reflectionWrapper.getFunction(nbtTagClass, "putInt", listOf(String::class, Int::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Int::class) },
        Version.V_17_FORGE.handleFunction("m_128405_") { setParameterClasses(String::class, Int::class) } // ~1.20.2
    )

    private val setLongFunction = reflectionWrapper.getFunction(nbtTagClass, "putLong", listOf(String::class, Long::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Long::class) },
        Version.V_17_FORGE.handleFunction("m_128356_") { setParameterClasses(String::class, Long::class) } // ~1.20.2
    )

    private val setFloatFunction = reflectionWrapper.getFunction(nbtTagClass, "putFloat", listOf(String::class, Float::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Float::class) },
        Version.V_17_FORGE.handleFunction("m_128350_") { setParameterClasses(String::class, Float::class) } // ~1.20.2
    )

    private val setDoubleFunction = reflectionWrapper.getFunction(nbtTagClass, "putDouble", listOf(String::class, Double::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, Double::class) },
        Version.V_17_FORGE.handleFunction("m_128347_") { setParameterClasses(String::class, Double::class) } // ~1.20.2
    )

    private val setByteArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "putByteArray", listOf(String::class, ByteArray::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, ByteArray::class) },
        Version.V_17_FORGE.handleFunction("m_128382_") { setParameterClasses(String::class, ByteArray::class) } // ~1.20.2
    )

    private val setIntArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "putIntArray", listOf(String::class, IntArray::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, IntArray::class) },
        Version.V_17_FORGE.handleFunction("m_128385_") { setParameterClasses(String::class, IntArray::class) } // ~1.20.2
    )

    private val setLongArrayFunction = reflectionWrapper.getFunction(nbtTagClass, "putLongArray", listOf(String::class, LongArray::class),
        Version.V_17.handleFunction("a") { setParameterClasses(String::class, LongArray::class) },
        Version.V_17_FORGE.handleFunction("m_128388_") { setParameterClasses(String::class, LongArray::class) } // ~1.20.2
    )

    private val removeFunction = reflectionWrapper.getFunction(nbtTagClass, "remove", listOf(String::class),
        Version.V_17.handleFunction("r") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128473_") { setParameterClasses(String::class) } // ~1.20.2
    )

    private val containsFunction = reflectionWrapper.getFunction(nbtTagClass, "contains", listOf(String::class),
        Version.V_17.handleFunction("e") { setParameterClasses(String::class) },
        Version.V_17_FORGE.handleFunction("m_128441_") { setParameterClasses(String::class) } // ~1.20.2
    )

    private val isEmptyFunction = reflectionWrapper.getFunction(
        nbtTagClass, "isEmpty",
        Version.V_17.handleFunction("f"),
        Version.V_19_3.handleFunction("g"),
        Version.V_17_FORGE.handleFunction("m_128456_") // ~1.20.2
    )

    fun getString(key: String, def: String = ""): String {
        return (getStringFunction.call(nbtTag, key) as? String)?.ifEmpty { def }?: def
    }

    fun getStringOrNull(key: String): String? {
        return if (hasKey(key)) return getString(key) else null
    }

    fun setString(key: String, value: String) {
        setStringFunction.call(nbtTag, key, value)
    }

    fun getBoolean(key: String, def: Boolean = false): Boolean {
        return getBooleanFunction.call(nbtTag, key) as? Boolean ?: def
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return if (hasKey(key)) return getBoolean(key) else null
    }

    fun setBoolean(key: String, value: Boolean) {
        setBooleanFunction.call(nbtTag, key, value)
    }

    fun getUUID(key: String): UUID {
        return getUUIDFunction.call(nbtTag, key) as UUID
    }

    fun getUUIDOrNull(key: String): UUID? {
        return if (hasKey(key)) return getUUID(key) else null
    }

    fun setUUID(key: String, value: UUID) {
        setUUIDFunction.call(nbtTag, key, value)
    }

    fun getByte(key: String, def: Byte = 0): Byte {
        return getByteFunction.call(nbtTag, key) as? Byte ?: def
    }

    fun getByteOrNull(key: String): Byte? {
        return if (hasKey(key)) getByte(key) else null
    }

    fun setByte(key: String, value: Byte) {
        setByteFunction.call(nbtTag, key, value)
    }

    fun getShort(key: String, def: Short = 0): Short {
        return getShortFunction.call(nbtTag, key) as? Short ?: def
    }

    fun getShortOrNull(key: String): Short? {
        return if (hasKey(key)) getShort(key) else null
    }

    fun setShort(key: String, value: Short) {
        setShortFunction.call(nbtTag, key, value)
    }

    fun getInt(key: String, def: Int = 0): Int {
        return getIntFunction.call(nbtTag, key) as? Int ?: def
    }

    fun getIntOrNull(key: String): Int? {
        return if (hasKey(key)) getInt(key) else null
    }

    fun setInt(key: String, value: Int) {
        setIntFunction.call(nbtTag, key, value)
    }

    fun getLong(key: String, def: Long = 0L): Long {
        return getLongFunction.call(nbtTag, key) as? Long ?: def
    }

    fun getLongOrNull(key: String): Long? {
        return if (hasKey(key)) getLong(key) else null
    }

    fun setLong(key: String, value: Long) {
        setLongFunction.call(nbtTag, key, value)
    }

    fun getFloat(key: String, def: Float = 0f): Float {
        return getFloatFunction.call(nbtTag, key) as? Float ?: def
    }

    fun getFloatOrNull(key: String): Float? {
        return if (hasKey(key)) getFloat(key) else null
    }

    fun setFloat(key: String, value: Float) {
        setFloatFunction.call(nbtTag, key, value)
    }

    fun getDouble(key: String, def: Double = 0.0): Double {
        return getDoubleFunction.call(nbtTag, key) as? Double ?: def
    }

    fun getDoubleOrNull(key: String): Double? {
        return if (hasKey(key)) getDouble(key) else null
    }

    fun setDouble(key: String, value: Double) {
        setDoubleFunction.call(nbtTag, key, value)
    }

    fun getByteArray(key: String, def: ByteArray = byteArrayOf()): ByteArray {
        return getByteArrayFunction.call(nbtTag, key) as? ByteArray ?: def
    }

    fun getByteArrayOrNull(key: String): ByteArray? {
        return if (hasKey(key)) getByteArray(key) else null
    }

    fun setByteArray(key: String, value: ByteArray) {
        setByteArrayFunction.call(nbtTag, key, value)
    }

    fun getIntArray(key: String, def: IntArray = intArrayOf()): IntArray {
        return getIntArrayFunction.call(nbtTag, key) as? IntArray ?: def
    }

    fun getIntArrayOrNull(key: String): IntArray? {
        return if (hasKey(key)) getIntArray(key) else null
    }

    fun setIntArray(key: String, value: IntArray) {
        setIntArrayFunction.call(nbtTag, key, value)
    }

    fun getLongArray(key: String, def: LongArray = longArrayOf()): LongArray {
        return getLongArrayFunction.call(nbtTag, key) as? LongArray ?: def
    }

    fun getLongArrayOrNull(key: String): LongArray? {
        return if (hasKey(key)) getLongArray(key) else null
    }

    fun setLongArray(key: String, value: LongArray) {
        setLongArrayFunction.call(nbtTag, key, value)
    }

    fun hasKey(key: String): Boolean {
        return containsFunction.call(nbtTag, key) as? Boolean ?: false
    }

    fun remove(key: String) {
        removeFunction.call(nbtTag, key)
    }

    fun isEmpty(): Boolean {
        return isEmptyFunction.call(nbtTag) as? Boolean ?: true
    }

    override fun getUnwrappedInstance(): Any {
        return nbtTag
    }
}