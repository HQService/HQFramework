package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class VirtualAnvilContainer(
    private val player: Player,
    private val title: BaseComponent
) : VirtualContainer(player, ComponentSerializer.toString(title)) {
    override fun createVirtualMessage(): VirtualMessage? {
        val container = containerService.wrap(player)

        val containerType = VirtualContainerType.getType(InventoryType.ANVIL, 3) ?: return null
        val virtualContainerType = containerType.getVirtualType(containersClass, reflectionWrapper.getFullVersion().ordinal >= Version.V_20_4.ordinal)

        val constructor = packetClass.java.getConstructor(
            Int::class.javaPrimitiveType,
            containersClass.java,
            baseComponentService.getTargetClass().java
        )
        return VirtualMessageImpl(
            constructor.newInstance(
                container.getContainerId(),
                virtualContainerType,
                baseComponentService.wrap(ComponentSerializer.toString(title)).getUnwrappedInstance()
            )
        )
    }
}