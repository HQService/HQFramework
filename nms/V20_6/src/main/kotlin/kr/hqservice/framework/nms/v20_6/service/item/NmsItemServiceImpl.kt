package kr.hqservice.framework.nms.v20_6.service.item

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.item.NmsItemService
import kr.hqservice.framework.nms.v20_6.wrapper.item.NmsItemWrapperImpl
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.inventory.CraftItemStack
import kotlin.reflect.KClass

class NmsItemServiceImpl(
    private val languageRegistry: LanguageRegistry
) : NmsItemService {
    override fun wrap(target: NmsItemStackWrapper): NmsItemWrapper {
        return NmsItemWrapperImpl(target, CraftItemStack.asNMSCopy(target.getBukkitItemStack()).item, languageRegistry)
    }

    override fun unwrap(wrapper: NmsItemWrapper): NmsItemStackWrapper {
        return (wrapper as NmsItemWrapperImpl).itemStackWrapper
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return Item::class
    }
}