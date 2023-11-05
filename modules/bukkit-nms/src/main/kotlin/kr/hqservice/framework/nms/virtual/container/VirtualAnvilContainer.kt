package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class VirtualAnvilContainer(
    private val player: Player,
    private val title: String
) : VirtualContainer(player, title) {
    override fun createVirtualMessage(): VirtualMessage? {
        val container = containerService.wrap(player)

        val containerType = VirtualContainerType.getType(InventoryType.ANVIL, 3) ?: return null
        val virtualContainerType = containerType.getVirtualType(containersClass)
        val constructor = packetClass.java.getConstructor(
            Int::class.javaPrimitiveType,
            containersClass.java,
            baseComponentService.getTargetClass().java
        )
        return VirtualMessageImpl(
            constructor.newInstance(
                container.getContainerId(),
                virtualContainerType,
                baseComponentService.wrap(title).getUnwrappedInstance()
            )
        )
    }
}