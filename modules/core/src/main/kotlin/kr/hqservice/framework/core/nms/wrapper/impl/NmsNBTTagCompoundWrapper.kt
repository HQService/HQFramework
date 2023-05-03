package kr.hqservice.framework.core.nms.wrapper.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.util.getFunction
import kr.hqservice.framework.core.nms.wrapper.NmsWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsNBTTagCompoundWrapper(
    internal val nbtTag: Any
) : NmsWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt"))

    private val getStringFunction = reflectionUtil.getFunction(nbtTagClass,"getString", listOf(String::class),
        Version.V_15.handle("l") { setParameterClasses(String::class) }
    )

    private val setStringFunction = reflectionUtil.getFunction(nbtTagClass, "setString", listOf(String::class, String::class),
        Version.V_15.handle("a") { setParameterClasses(String::class, String::class) }
    )

    fun getString(key: String): String? {
        return getStringFunction.call(nbtTag, key) as? String?
    }

    fun setString(key: String, value: String) {
        setStringFunction.call(nbtTag, key, value)
    }

}