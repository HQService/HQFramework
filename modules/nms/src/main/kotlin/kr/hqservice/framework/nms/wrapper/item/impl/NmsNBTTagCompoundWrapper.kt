package kr.hqservice.framework.nms.wrapper.item.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.item.ItemWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsNBTTagCompoundWrapper(
    internal val nbtTag: Any,
) : ItemWrapper, KoinComponent {
    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_15.handle("nbt"))

    private val getStringFunction = reflectionUtil.getFunction(nbtTagClass, "getString", listOf(String::class),
        Version.V_15.handleFunction("l") { setParameterClasses(String::class) })

    private val setStringFunction =
        reflectionUtil.getFunction(nbtTagClass, "setString", listOf(String::class, String::class),
            Version.V_15.handleFunction("a") { setParameterClasses(String::class, String::class) })

    private val removeFunction = reflectionUtil.getFunction(nbtTagClass, "remove", listOf(String::class),
        Version.V_15.handleFunction("r") { setParameterClasses(String::class) })

    private val containsFunction = reflectionUtil.getFunction(nbtTagClass, "contains", listOf(String::class),
        Version.V_15.handleFunction("e") { setParameterClasses(String::class) })

    fun getString(key: String): String? {
        return getStringFunction.call(nbtTag, key) as? String?
    }

    fun setString(key: String, value: String) {
        setStringFunction.call(nbtTag, key, value)
    }

    fun contains(key: String): Boolean {
        return containsFunction.call(nbtTag, key) as? Boolean?: false
    }

    fun remove(key: String) {
        removeFunction.call(nbtTag, key)
    }
}