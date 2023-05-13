package kr.hqservice.framework.nms.wrapper.item.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.impl.NmsItemService
import kr.hqservice.framework.nms.service.impl.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.item.ItemWrapper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class NmsItemStackWrapper(
    internal val nmsItemStack: Any
) : ItemWrapper, KoinComponent {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val tagService: NmsNBTTagCompoundService by inject(named("tag"))
    private val itemService: NmsItemService by inject(named("item"))

    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))
    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound", Version.V_15.handle("nbt"))

    private val getTagFunction = reflectionUtil.getFunction(nmsItemStackClass, "getTag",
        Version.V_15.handle("o"),
        Version.V_17.handle("s"),
        Version.V_19.handle("u"),
    )

    private val setTagFunction = reflectionUtil.getFunction(nmsItemStackClass, "setTag", listOf(nbtTagClass),
        Version.V_15.handleFunction("c") { setParameterClasses(nbtTagClass) }
    )

    fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        return tagService.wrap(getTagFunction.call(nmsItemStack)?: return null)
    }

    fun getTag(): NmsNBTTagCompoundWrapper {
        return tagService.wrap(getTagFunction.call(nmsItemStack))
    }

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {
        setTagFunction.call(nmsItemStack, nbtTagCompoundWrapper?.run(tagService::unwrap))
    }

    fun getItem(): NmsItemWrapper {
        return itemService.wrap(this)
    }

}