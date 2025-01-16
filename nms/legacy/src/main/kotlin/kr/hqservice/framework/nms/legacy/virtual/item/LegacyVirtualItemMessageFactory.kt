package kr.hqservice.framework.nms.legacy.virtual.item

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class LegacyVirtualItemMessageFactory(
    reflectionWrapper: LegacyNmsReflectionWrapper,
    private val itemStackService: NmsItemStackService,
    private val containerService: NmsContainerService
) : VirtualItemMessageFactory {
    private val packetClass = reflectionWrapper.getNmsClass("PacketPlayOutSetSlot",
        Version.V_17.handle("network.protocol.game")
    )

    override fun create(
        player: Player,
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit
    ): VirtualMessage {
        val container = containerService.wrap(player)
        val nmsItemStack =
            if (itemStack.type == Material.AIR) {
                itemStackService.wrap(itemStack)
            } else itemStackService.wrap(
                itemStack.clone().apply {
                    itemMeta = itemMeta?.apply(itemEditBlock)
                }
            )
        val message = packetClass.java.getConstructor(
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            itemStackService.getTargetClass().java
        ).newInstance(container.getContainerId(), container.getStateId(), slot, nmsItemStack.getUnwrappedInstance())
        return VirtualMessageImpl(message)
    }
}