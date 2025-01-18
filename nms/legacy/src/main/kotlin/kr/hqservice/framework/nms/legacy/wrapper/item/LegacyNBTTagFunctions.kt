package kr.hqservice.framework.nms.legacy.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import java.util.*

class LegacyNBTTagFunctions(
    private val reflectionWrapper: LegacyNmsReflectionWrapper
) {
    private val nbtTagClass by lazy { reflectionWrapper.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt")) }

    val getStringFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "getString", listOf(String::class),
            Version.V_18.handleFunction("l") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128461_") { setParameterClasses(String::class) } // ~1.20.2
        )
    }

    val getBooleanFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getBoolean", listOf(String::class),
            Version.V_18.handleFunction("q") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128471_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getUUIDFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getUUID", listOf(String::class),
            Version.V_17.handleFunction("a") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128342_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getByteFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getByte", listOf(String::class),
            Version.V_18.handleFunction("f") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128445_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getShortFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getShort", listOf(String::class),
            Version.V_18.handleFunction("g") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128448_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getIntFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "getInt", listOf(String::class),
            Version.V_18.handleFunction("h") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128451_") { setParameterClasses(String::class) } // ~1.20.2
        )
    }

    val getLongFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getLong", listOf(String::class),
            Version.V_18.handleFunction("i") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128454_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getFloatFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getFloat", listOf(String::class),
            Version.V_18.handleFunction("j") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128457_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getDoubleFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getDouble", listOf(String::class),
            Version.V_18.handleFunction("k") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128459_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getByteArrayFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getByteArray", listOf(String::class),
            Version.V_18.handleFunction("m") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128463_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getIntArrayFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getIntArray", listOf(String::class),
            Version.V_18.handleFunction("n") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128465_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val getLongArrayFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "getLongArray", listOf(String::class),
            Version.V_18.handleFunction("o") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128467_") { setParameterClasses(String::class) }, // ~1.20.2
        )
    }

    val setStringFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setString", listOf(String::class, String::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, String::class) },
            Version.V_17_FORGE.handleFunction("m_128359_") {
                setParameterClasses(
                    String::class,
                    String::class
                )
            } // ~1.20.2
        )
    }

    val setBooleanFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setBoolean", listOf(String::class, Boolean::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Boolean::class) },
            Version.V_17_FORGE.handleFunction("m_128379_") {
                setParameterClasses(
                    String::class,
                    Boolean::class
                )
            } // ~1.20.2
        )
    }

    val setUUIDFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setUUID", listOf(String::class, UUID::class),
            Version.V_17.handleFunction("a") { setParameterClasses(String::class, UUID::class) },
            Version.V_17_FORGE.handleFunction("m_128362_") {
                setParameterClasses(
                    String::class,
                    UUID::class
                )
            } // ~1.20.2
        )
    }

    val setByteFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setByte", listOf(String::class, Byte::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Byte::class) },
            Version.V_17_FORGE.handleFunction("m_128344_") {
                setParameterClasses(
                    String::class,
                    Byte::class
                )
            } // ~1.20.2
        )
    }

    val setShortFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setShort", listOf(String::class, Short::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Short::class) },
            Version.V_17_FORGE.handleFunction("m_128376_") {
                setParameterClasses(
                    String::class,
                    Short::class
                )
            } // ~1.20.2
        )
    }

    val setIntFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setInt", listOf(String::class, Int::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Int::class) },
            Version.V_17_FORGE.handleFunction("m_128405_") { setParameterClasses(String::class, Int::class) } // ~1.20.2
        )
    }

    val setLongFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setLong", listOf(String::class, Long::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Long::class) },
            Version.V_17_FORGE.handleFunction("m_128356_") {
                setParameterClasses(
                    String::class,
                    Long::class
                )
            } // ~1.20.2
        )
    }

    val setFloatFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setFloat", listOf(String::class, Float::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Float::class) },
            Version.V_17_FORGE.handleFunction("m_128350_") {
                setParameterClasses(
                    String::class,
                    Float::class
                )
            } // ~1.20.2
        )
    }

    val setDoubleFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setDouble", listOf(String::class, Double::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, Double::class) },
            Version.V_17_FORGE.handleFunction("m_128347_") {
                setParameterClasses(
                    String::class,
                    Double::class
                )
            } // ~1.20.2
        )
    }

    val setByteArrayFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setByteArray", listOf(String::class, ByteArray::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, ByteArray::class) },
            Version.V_17_FORGE.handleFunction("m_128382_") {
                setParameterClasses(
                    String::class,
                    ByteArray::class
                )
            } // ~1.20.2
        )
    }

    val setIntArrayFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setIntArray", listOf(String::class, IntArray::class),
            Version.V_18.handleFunction("a") { setParameterClasses(String::class, IntArray::class) },
            Version.V_17_FORGE.handleFunction("m_128385_") {
                setParameterClasses(
                    String::class,
                    IntArray::class
                )
            } // ~1.20.2
        )
    }

    val setLongArrayFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "setLongArray", listOf(String::class, LongArray::class),
            Version.V_17.handleFunction("a") { setParameterClasses(String::class, LongArray::class) },
            Version.V_17_FORGE.handleFunction("m_128388_") {
                setParameterClasses(
                    String::class,
                    LongArray::class
                )
            } // ~1.20.2
        )
    }

    val removeFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "remove", listOf(String::class),
            Version.V_18.handleFunction("r") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128473_") { setParameterClasses(String::class) } // ~1.20.2
        )
    }

    val containsFunction by lazy {
        reflectionWrapper.getFunction(nbtTagClass, "hasKey", listOf(String::class),
            Version.V_18.handleFunction("e") { setParameterClasses(String::class) },
            Version.V_17_FORGE.handleFunction("m_128441_") { setParameterClasses(String::class) } // ~1.20.2
        )
    }

    val isEmptyFunction by lazy {
        reflectionWrapper.getFunction(
            nbtTagClass, "isEmpty",
            Version.V_18.handleFunction("f"),
            Version.V_19_3.handleFunction("g"),
            Version.V_17_FORGE.handleFunction("m_128456_") // ~1.20.2
        )
    }
}