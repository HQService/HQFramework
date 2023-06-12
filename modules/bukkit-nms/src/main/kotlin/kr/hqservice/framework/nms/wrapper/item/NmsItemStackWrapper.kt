package kr.hqservice.framework.nms.wrapper.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.service.impl.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.NmsWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class NmsItemStackWrapper(
    private val nmsItemStack: Any,
) : NmsWrapper, KoinComponent {
    private val reflectionUtil: NmsReflectionUtil by inject()

    private val tagService: NmsNBTTagCompoundService by inject(named("tag"))
    private val itemService: NmsService<NmsItemStackWrapper, NmsItemWrapper> by inject(named("item"))
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper> by inject(named("itemStack"))

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
        return getTag() != null
    }

    fun tag(tagScope: NmsNBTTagCompoundWrapper.() -> Unit = {}): NmsNBTTagCompoundWrapper {
        val tag = getTag()?: createNewTag()
        tag.tagScope()
        setTag(tag)
        return tag
    }

    fun getTag(): NmsNBTTagCompoundWrapper? {
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