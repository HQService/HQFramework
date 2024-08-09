package kr.hqservice.framework.nms.service.item

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

@Qualifier("itemStack")
@Service
class NmsItemStackService(
    private val languageRegistry: LanguageRegistry
) : NmsService<ItemStack, NmsItemStackWrapper> {
    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return NmsItemStackWrapper(CraftItemStack.asNMSCopy(target))
    }

    override fun unwrap(wrapper: NmsItemStackWrapper): ItemStack {
        return (wrapper.getBukkitItemStack() as net.minecraft.world.item.ItemStack).asBukkitCopy()
    }

    fun getItem(target: ItemStack): NmsItemWrapper {
        return NmsItemWrapper(
            wrap(target),
            (CraftItemStack.asNMSCopy(target)).item,
            languageRegistry
        )
    }

    override fun getWrapper(nmsInstance: Any): NmsItemStackWrapper {
        return NmsItemStackWrapper(nmsInstance as net.minecraft.world.item.ItemStack)
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return net.minecraft.world.item.ItemStack::class
    }
}
