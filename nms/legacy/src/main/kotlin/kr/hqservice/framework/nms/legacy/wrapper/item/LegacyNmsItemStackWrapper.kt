package kr.hqservice.framework.nms.legacy.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import org.bukkit.inventory.ItemStack
import org.koin.java.KoinJavaComponent.getKoin

class LegacyNmsItemStackWrapper(
    private val nmsItemStack: Any,
    private val tagService: NmsNBTTagCompoundService,
    private val itemService: NmsItemService,
    private val itemStackService: NmsItemStackService
) : NmsItemStackWrapper {
    companion object {
        private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()

        private val nmsItemStackClass by lazy { reflectionWrapper.getNmsClass("ItemStack", Version.V_17.handle("world.item")) }
        private val nbtTagClass by lazy { reflectionWrapper.getNmsClass("NBTTagCompound", Version.V_17.handle("nbt")) }

        private val getTagFunction by lazy { reflectionWrapper.getFunction(nmsItemStackClass, "getTag",
            Version.V_18.handle("s"),
            Version.V_18_2.handle("t"),
            Version.V_19.handle("u"),
            Version.V_20.handle("v"),
            Version.V_20_4.handle("v"),
            Version.V_17_FORGE.handle("m_41783_") // ~1.20.2
        ) }

        private val setTagFunction by lazy { reflectionWrapper.getFunction(nmsItemStackClass, "setTag", listOf(nbtTagClass),
            Version.V_18.handleFunction("c") { setParameterClasses(nbtTagClass) },
            Version.V_17_FORGE.handleFunction("m_41751_") { setParameterClasses(nbtTagClass) }, // ~1.20.2
        ) }
    }

    override fun hasTag(): Boolean {
        return getTagOrNull() != null
    }

    override fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit): NmsNBTTagCompoundWrapper {
        val tag = getTagOrNull() ?: createNewTag()
        tag.tagScope()
        setTag(tag)
        return tag
    }

    override fun getTag(): NmsNBTTagCompoundWrapper {
        return getTagFunction.call(nmsItemStack)?.run(tagService::wrap) ?: throw NullPointerException("nbt-tag is null")
    }

    override fun getTagOrNull(): NmsNBTTagCompoundWrapper? {
        return getTagFunction.call(nmsItemStack)?.run(tagService::wrap)
    }

    private fun createNewTag(): NmsNBTTagCompoundWrapper {
        return tagService.wrap(nbtTagClass.java.getConstructor().newInstance())
    }

    override fun setTag(nbtTagCompoundWrapper: NmsNBTTagCompoundWrapper?) {
        setTagFunction.call(nmsItemStack, nbtTagCompoundWrapper?.run(tagService::unwrap))
    }

    override fun getItem(): NmsItemWrapper {
        return itemService.wrap(this)
    }

    override fun getBukkitItemStack(): ItemStack {
        return itemStackService.unwrap(this)
    }

    override fun getUnwrappedInstance(): Any {
        return nmsItemStack
    }
}