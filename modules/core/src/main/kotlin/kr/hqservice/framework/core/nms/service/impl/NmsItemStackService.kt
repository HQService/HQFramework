package kr.hqservice.framework.core.nms.service.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.service.NmsService
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.util.getFunction
import kr.hqservice.framework.core.nms.util.getStaticFunction
import kr.hqservice.framework.core.nms.wrapper.impl.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Component
@Named("item")
@HQSingleton(binds = [NmsService::class])
class NmsItemStackService(
    reflectionUtil: NmsReflectionUtil
) : KoinComponent, NmsService<ItemStack, NmsItemStackWrapper>, HQService {

    private val craftItemStackClass = reflectionUtil.getCraftBukkitClass("inventory.CraftItemStack")
    private val nmsItemStackClass = reflectionUtil.getNmsClass("ItemStack", Version.V_15.handle("world.item"))

    private val asNmsCopyFunction = reflectionUtil.getStaticFunction(craftItemStackClass, "asNMSCopy", nmsItemStackClass, listOf(ItemStack::class))
    private val asBukkitCopyFunction = reflectionUtil.getStaticFunction(craftItemStackClass, "asBukkitCopy", ItemStack::class, listOf(nmsItemStackClass))

    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return NmsItemStackWrapper(asNmsCopyFunction.call(target)?: throw IllegalArgumentException())
    }

    override fun unwrap(wrapper: NmsItemStackWrapper): ItemStack {
        return asBukkitCopyFunction.call(wrapper.nmsItemStack) as? ItemStack?: throw IllegalArgumentException()
    }

}
