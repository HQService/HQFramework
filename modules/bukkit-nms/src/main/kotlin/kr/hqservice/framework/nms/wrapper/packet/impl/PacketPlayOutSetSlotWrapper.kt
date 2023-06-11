package kr.hqservice.framework.nms.wrapper.packet.impl

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.wrapper.container.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.item.impl.NmsItemStackWrapper
import kr.hqservice.framework.nms.wrapper.packet.NmsPacketWrapper
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.reflect.KClass

class PacketPlayOutSetSlotWrapper(
    private val player: Player,
    private val slot: Int,
    private val itemStack: ItemStack,
    private val virtualBlock: (ItemStack) -> Unit = {},
) : NmsPacketWrapper(), KoinComponent {
    private val reflectionUtil: NmsReflectionUtil by inject()
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper> by inject(named("itemStack"))
    private val containerService: NmsService<Player, ContainerWrapper> by inject(named("container"))
    private val packetClass =
        reflectionUtil.getNmsClass("PacketPlayOutSetSlot", Version.V_15.handle("network.protocol.game"))

    override fun getClass(): KClass<*> {
        return packetClass
    }

    override fun createPacket(): Any {
        val container = containerService.wrap(player)
        val nmsItemStack =
            if (itemStack.type == Material.AIR) {
                itemStackService.wrap(itemStack)
            } else itemStackService.wrap(
                itemStack.clone().apply(virtualBlock)
            )

        return packetClass.java.getConstructor(
            Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, itemStackService.getTargetClass().java
        ).newInstance(container.getContainerId(), container.getStateId(), slot, nmsItemStack.nmsItemStack)

    }
}