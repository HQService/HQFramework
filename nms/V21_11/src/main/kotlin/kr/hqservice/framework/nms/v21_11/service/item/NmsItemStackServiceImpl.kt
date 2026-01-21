package kr.hqservice.framework.nms.v21_11.service.item

import kr.hqservice.framework.nms.registry.LanguageRegistry
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.v21_11.wrapper.item.NmsItemStackWrapperImpl
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemWrapper
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

class NmsItemStackServiceImpl(
    private val languageRegistry: LanguageRegistry
) : NmsItemStackService {
    override fun wrap(target: ItemStack): NmsItemStackWrapper {
        return NmsItemStackWrapperImpl(languageRegistry, CraftItemStack.asNMSCopy(target))
    }

    override fun unwrap(wrapper: NmsItemStackWrapper): ItemStack {
        return wrapper.getBukkitItemStack()
    }

    override fun getWrapper(nmsInstance: Any): NmsItemStackWrapper {
        return NmsItemStackWrapperImpl(languageRegistry, nmsInstance as net.minecraft.world.item.ItemStack)
    }

    override fun getOriginalClass(): KClass<*> {
        return ItemStack::class
    }

    override fun getTargetClass(): KClass<*> {
        return net.minecraft.world.item.ItemStack::class
    }
}