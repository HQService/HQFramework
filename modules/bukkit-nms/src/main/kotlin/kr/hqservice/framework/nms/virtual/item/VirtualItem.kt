package kr.hqservice.framework.nms.virtual.item

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualItem(
    private val player: Player,
    private val slot: Int,
    private val itemStack: ItemStack,
    private val itemEditBlock: ItemMeta.() -> Unit = {},
) : Virtual, KoinComponent {
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper> by inject(named("itemStack"))
    private val containerService: NmsService<Player, ContainerWrapper> by inject(named("container"))

    override fun createVirtualMessage(): VirtualMessage {
        val container = containerService.wrap(player)
        val nmsItemStack =
            if (itemStack.type == Material.AIR) {
                itemStackService.wrap(itemStack)
            } else itemStackService.wrap(
                itemStack.clone().apply {
                    itemMeta = itemMeta?.apply(itemEditBlock)
                }
            )
        val message = ClientboundContainerSetSlotPacket(container.getContainerId(), container.getStateId(), slot, nmsItemStack.getUnwrappedInstance() as net.minecraft.world.item.ItemStack)
        return VirtualMessageImpl(message)
    }
}