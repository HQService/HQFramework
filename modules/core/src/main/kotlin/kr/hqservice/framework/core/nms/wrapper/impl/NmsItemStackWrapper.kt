package kr.hqservice.framework.core.nms.wrapper.impl

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.service.impl.NmsNBTTagCompoundService
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.util.getFunction
import kr.hqservice.framework.core.nms.wrapper.NmsWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class NmsItemStackWrapper(
    val nmsItemStack: Any
) : NmsWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val tagService: NmsNBTTagCompoundService by inject(named("tag"))

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_17.handle("world.item"))
    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt.NBTTagCompound"))

    private val getTagFunction = reflectionUtil.getFunction(nmsItemStackClass, "getTag", Version.V_17.handle("u"))
    private val setTagFunction = reflectionUtil.getFunction(nmsItemStackClass, "setTag", listOf(nbtTagClass), Version.V_17.handle("c"))

    fun getTag(): NmsNBTTagCompoundWrapper {
        return tagService.wrap(getTagFunction.call(nmsItemStack))
    }

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper) {
        setTagFunction.call(tagService.unwrap(nbtTagCompoundWrapper))
    }

}