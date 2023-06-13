package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.inventory.ItemStack

class NmsItemStackWrapper(
    private val nmsItemStack: Any,
    reflectionUtil: NmsReflectionWrapper,
    private val tagService: NmsNBTTagCompoundService,
    private val itemService: NmsService<NmsItemStackWrapper, NmsItemWrapper>,
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>
) : NmsWrapper {
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

    fun hasTag(): Boolean {
        return getTagOrNull() != null
    }

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit = {}): NmsNBTTagCompoundWrapper {
        val tag = getTagOrNull()?: createNewTag()
        tag.tagScope()
        setTag(tag)
        return tag
    }

    fun getTag(): NmsNBTTagCompoundWrapper {
        return getTagFunction.call(nmsItemStack)?.run(tagService::wrap)?: throw NullPointerException("nbt-tag is null")
    }

    fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        return getTagFunction.call(nmsItemStack)?.run(tagService::wrap)
    }

    private fun createNewTag(): NmsNBTTagCompoundWrapper {
        return tagService.wrap(nbtTagClass.java.getConstructor().newInstance())
    }

    fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {
        setTagFunction.call(nmsItemStack, nbtTagCompoundWrapper?.run(tagService::unwrap))
    }

    fun getItem(): NmsItemWrapper {
        return itemService.wrap(this)
    }

    fun getBukkitItemStack(): ItemStack {
        return itemStackService.unwrap(this)
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItemStack
    }
}