package kr.hqservice.framework.nms.service.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQService
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getStaticFunction
import kr.hqservice.framework.nms.wrapper.item.impl.NmsItemStackWrapper
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent

@Component
@Named("itemStack")
@Singleton(binds = [NmsService::class])
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
