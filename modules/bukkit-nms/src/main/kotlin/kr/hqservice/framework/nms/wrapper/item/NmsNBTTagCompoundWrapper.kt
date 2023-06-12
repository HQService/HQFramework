package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsNBTTagCompoundWrapper(
    private val nbtTag: Any,
) : NmsWrapper, KoinComponent {
    private val reflectionUtil: NmsReflectionUtil by inject()
    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_15.handle("nbt"))

    private val getStringFunction = reflectionUtil.getFunction(nbtTagClass, "getString", listOf(String::class),
        Version.V_15.handleFunction("l") { setParameterClasses(String::class) })

    private val getIntFunction = reflectionUtil.getFunction(nbtTagClass, "getInt", listOf(String::class),
        Version.V_15.handleFunction("i") { setParameterClasses(String::class)},
        Version.V_17.handleFunction("h") { setParameterClasses(String::class)})

    private val setIntFunction = reflectionUtil.getFunction(nbtTagClass, "setInt", listOf(String::class, Int::class),
        Version.V_15.handleFunction("a") { setParameterClasses(String::class, Int::class)} )

    private val setStringFunction = reflectionUtil.getFunction(nbtTagClass, "setString", listOf(String::class, String::class),
        Version.V_15.handleFunction("a") { setParameterClasses(String::class, String::class) })

    private val removeFunction = reflectionUtil.getFunction(nbtTagClass, "remove", listOf(String::class),
        Version.V_15.handleFunction("r") { setParameterClasses(String::class) })

    private val containsFunction = reflectionUtil.getFunction(nbtTagClass, "contains", listOf(String::class),
        Version.V_15.handleFunction("e") { setParameterClasses(String::class) })

    private val isEmptyFunction = reflectionUtil.getFunction(nbtTagClass, "isEmpty",
        Version.V_15.handleFunction("f"),
        Version.V_19_3.handleFunction("g"))

    fun getString(key: String, def: String = ""): String {
        return getStringFunction.call(nbtTag, key) as? String?: def
    }

    fun getStringOrNull(key: String): String? {
        return getStringFunction.call(nbtTag, key) as? String?
    }

    fun setString(key: String, value: String) {
        setStringFunction.call(nbtTag, key, value)
    }

    fun getInt(key: String, def: Int = 0): Int {
        return getIntFunction.call(nbtTag, key) as? Int?: def
    }

    fun getIntOrNull(key: String): Int? {
        return getIntFunction.call(nbtTag, key) as? Int?
    }

    fun setInt(key: String, value: Int) {
        setIntFunction.call(nbtTag, key, value)
    }

    fun hasKey(key: String): Boolean {
        return containsFunction.call(nbtTag, key) as? Boolean?: false
    }

    fun remove(key: String) {
        removeFunction.call(nbtTag, key)
    }

    fun isEmpty(): Boolean {
        return isEmptyFunction.call(nbtTag) as? Boolean?: true
    }

    override fun getUnwrappedInstance(): Any {
        return nbtTag
    }
}