package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getStaticFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("itemStack")
@HQSingleton(binds = [NmsService::class])
class NmsItemStackService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Named("tag") private val tagService: NmsNBTTagCompoundService,
    @Named("item") private val itemService: NmsService<NmsItemStackWrapper, NmsItemWrapper>,
    @Named("itemStack") private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>
) : NmsService<ItemStack, NmsItemStackWrapper> {
    private val craftItemStackClass = reflectionWrapper.getCraftBukkitClass("inventory.CraftItemStack")
    private val nmsItemStackClass = reflectionWrapper.getNmsClass("ItemStack", Version.V_15.handle("world.item"))

    private val asNmsCopyFunction = reflectionWrapper.getStaticFunction(craftItemStackClass, "asNMSCopy", nmsItemStackClass, listOf(ItemStack::class))
    private val asBukkitCopyFunction = reflectionWrapper.getStaticFunction(craftItemStackClass, "asBukkitCopy", ItemStack::class, listOf(nmsItemStackClass))

    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return NmsItemStackWrapper(
            asNmsCopyFunction.call(target)?: throw IllegalArgumentException(),
            reflectionWrapper,
            tagService, itemService, itemStackService
        )
    }

    override fun unwrap(wrapper: NmsItemStackWrapper): ItemStack {
        return asBukkitCopyFunction.call(wrapper.getUnwrappedInstance()) as? ItemStack?: throw IllegalArgumentException()
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return nmsItemStackClass
    }
}
