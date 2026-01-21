package kr.hqservice.framework.nms.v21_11.virtual.item

import kr.hqservice.framework.nms.service.container.NmsContainerService
import kr.hqservice.framework.nms.service.item.NmsItemStackService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.item.VirtualItemMessageFactory
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class VirtualItemMessageFactoryImpl(
    private val itemStackService: NmsItemStackService,
    private val containerService: NmsContainerService
) : VirtualItemMessageFactory {
    override fun create(
        player: Player,
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit
    ): VirtualMessage {
        val container = containerService.wrap(player)
        if (itemStack.type == Material.AIR) {
            val message = ClientboundContainerSetSlotPacket(
                container.getContainerId(),
                container.getStateId(),
                slot,
                CraftItemStack.asNMSCopy(itemStack)
            )
            return VirtualMessageImpl(message)
        } else {
            val nmsItemStack = itemStackService.wrap(itemStack.clone().apply { itemMeta = itemMeta?.apply(itemEditBlock) })

            val message = ClientboundContainerSetSlotPacket(
                container.getContainerId(),
                container.getStateId(),
                slot,
                nmsItemStack.getUnwrappedInstance() as net.minecraft.world.item.ItemStack
            )
            return VirtualMessageImpl(message)
        }
    }
}