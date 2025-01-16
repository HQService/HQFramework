package kr.hqservice.framework.nms.legacy.service.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.item.LegacyNmsItemStackWrapper
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.service.item.NmsNBTTagCompoundService
import kr.hqservice.framework.nms.legacy.wrapper.getStaticFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

class LegacyNMSItemStackService(
    private val tagService: NmsNBTTagCompoundService,
    private val itemService: NmsItemService,
) : NmsItemStackService {

    companion object {
        private val reflectionWrapper: LegacyNmsReflectionWrapper by getKoin().inject()

        private val craftItemStackClass by lazy { reflectionWrapper.getCraftBukkitClass("inventory.CraftItemStack") }
        private val nmsItemStackClass  by lazy { reflectionWrapper.getNmsClass("ItemStack",
            Version.V_17.handle("world.item")
        ) }

        private val asNmsCopyFunction  by lazy { reflectionWrapper.getStaticFunction(
            craftItemStackClass,
            "asNMSCopy",
            nmsItemStackClass,
            listOf(ItemStack::class)
        ) }

        private val asBukkitCopyFunction  by lazy { reflectionWrapper.getStaticFunction(
            craftItemStackClass,
            "asBukkitCopy",
            ItemStack::class,
            listOf(nmsItemStackClass)
        ) }
    }

    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return LegacyNmsItemStackWrapper(
            asNmsCopyFunction.call(target) ?: throw IllegalArgumentException(),
            tagService,
            itemService, this
        )
    }

    override fun unwrap(wrapper: NmsItemStackWrapper): ItemStack {
        return asBukkitCopyFunction.call(wrapper.getUnwrappedInstance()) as? ItemStack
            ?: throw IllegalArgumentException()
    }

    override fun getWrapper(nmsInstance: Any): NmsItemStackWrapper {
        return LegacyNmsItemStackWrapper(nmsInstance, tagService, itemService, this)
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return nmsItemStackClass
    }
}
