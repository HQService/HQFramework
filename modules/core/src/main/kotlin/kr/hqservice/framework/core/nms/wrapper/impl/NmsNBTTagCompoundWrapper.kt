package kr.hqservice.framework.core.nms.wrapper.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.util.getFunction
import kr.hqservice.framework.core.nms.wrapper.NmsWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NmsNBTTagCompoundWrapper(
    val nbtTag: Any
) : NmsWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt.NBTTagCompound"))

    private val getStringFunction = reflectionUtil.getFunction(nbtTagClass,"getString", listOf(String::class), Version.V_17.handle("l") {
        listOf(String::class)
    })

    private val setStringFunction = reflectionUtil.getFunction(nbtTagClass, "setString", listOf(String::class, String::class), Version.V_17.handle("a") {
        setParameterClasses()
    })


}