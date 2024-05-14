package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getStaticFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsNBTTagCompoundWrapper
import org.bukkit.inventory.ItemStack
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.reflect.KClass

@Qualifier("itemStack")
@Service
class NmsItemStackService(
    @Qualifier("tag") private val tagService: NmsService<Any?, NmsNBTTagCompoundWrapper>,
    @Qualifier("item") private val itemService: NmsService<NmsItemStackWrapper, NmsItemWrapper>,
) : NmsService<ItemStack, NmsItemStackWrapper> {

    companion object {
        private val reflectionWrapper: NmsReflectionWrapper by getKoin().inject()

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
        return NmsItemStackWrapper(
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
        return NmsItemStackWrapper(nmsInstance, tagService, itemService, this)
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return nmsItemStackClass
    }
}
