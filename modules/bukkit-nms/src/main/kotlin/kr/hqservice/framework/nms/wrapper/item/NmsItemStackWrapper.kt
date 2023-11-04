package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import org.bukkit.inventory.ItemStack

class NmsItemStackWrapper(
    private val nmsItemStack: Any,
    reflectionWrapper: NmsReflectionWrapper,
    private val tagService: NmsService<Any?, NmsNBTTagCompoundWrapper>,
    private val itemService: NmsService<NmsItemStackWrapper, NmsItemWrapper>,
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>
) : NmsWrapper {

    private val nmsItemStackClass = reflectionWrapper.getNmsClass("ItemStack", Version.V_17.handle("world.item"))
    private val nbtTagClass = reflectionWrapper.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt"))

    private val getTagFunction = reflectionWrapper.getFunction(
        nmsItemStackClass, "getTag",
        Version.V_17.handle("o"),
        Version.V_17.handle("s"),
        Version.V_18_2.handle("t"),
        Version.V_19.handle("u"),
        Version.V_20.handle("v"),
        Version.V_17_FORGE.handle("m_41783_") // ~1.20.2
    )

    private val setTagFunction = reflectionWrapper.getFunction(nmsItemStackClass, "setTag", listOf(nbtTagClass),
        Version.V_17.handleFunction("c") { setParameterClasses(nbtTagClass) },
        Version.V_17_FORGE.handleFunction("m_41751_") { setParameterClasses(nbtTagClass) }, // ~1.20.2
    )

    fun hasTag(): Boolean {
        return getTagOrNull() != null
    }

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit = {}): NmsNBTTagCompoundWrapper {
        val tag = getTagOrNull() ?: createNewTag()
        tag.tagScope()
        setTag(tag)
        return tag
    }

    fun getTag(): NmsNBTTagCompoundWrapper {
        return getTagFunction.call(nmsItemStack)?.run(tagService::wrap) ?: throw NullPointerException("nbt-tag is null")
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