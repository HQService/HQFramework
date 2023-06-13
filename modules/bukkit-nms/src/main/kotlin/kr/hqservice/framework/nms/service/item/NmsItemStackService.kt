package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getStaticFunction
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@Component
@Named("itemStack")
@HQSingleton(binds = [NmsService::class])
class NmsItemStackService(
    reflectionUtil: NmsReflectionUtil
) : KoinComponent, NmsService<ItemStack, NmsItemStackWrapper> {
    private val craftItemStackClass = reflectionUtil.getCraftBukkitClass("inventory.CraftItemStack")
    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))

    private val asNmsCopyFunction = reflectionUtil.getStaticFunction(craftItemStackClass, "asNMSCopy", nmsItemStackClass, listOf(ItemStack::class))
    private val asBukkitCopyFunction = reflectionUtil.getStaticFunction(craftItemStackClass, "asBukkitCopy", ItemStack::class, listOf(nmsItemStackClass))

    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return NmsItemStackWrapper(asNmsCopyFunction.call(target)?: throw IllegalArgumentException())
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
