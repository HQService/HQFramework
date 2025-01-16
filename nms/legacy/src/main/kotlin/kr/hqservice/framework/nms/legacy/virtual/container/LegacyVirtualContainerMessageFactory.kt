package kr.hqservice.framework.nms.legacy.virtual.container

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.container.VirtualAnvilContainer
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.virtual.container.VirtualContainerMessageFactory
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView

class LegacyVirtualContainerMessageFactory(
    private val reflectionWrapper: LegacyNmsReflectionWrapper,
    private val baseComponentService: NmsService<String, BaseComponentWrapper>,
    private val containerService: NmsService<Player, ContainerWrapper>
) : VirtualContainerMessageFactory {
    private val containersClass = reflectionWrapper.getNmsClass("Containers",
        Version.V_17.handle("world.inventory")
    )
    private val packetClass = reflectionWrapper.getNmsClass("PacketPlayOutOpenWindow",
        Version.V_17.handle("network.protocol.game")
    )
    private val bukkitViewFunction = reflectionWrapper.getFunction(containerService.getTargetClass(), "getBukkitView")

    override fun create(virtualContainer: VirtualContainer): VirtualMessage? {
        if (virtualContainer is VirtualAnvilContainer)
            return createAnvil(virtualContainer)

        return createDefault(virtualContainer)
    }

    private fun createDefault(virtualContainer: VirtualContainer): VirtualMessage?{
        val container = containerService.wrap(virtualContainer.player)

        val bukkitView = bukkitViewFunction.call(container.getUnwrappedInstance())
        if (bukkitView !is InventoryView) throw UnsupportedOperationException("error1")

        val type = bukkitView.topInventory.type
        val size = bukkitView.topInventory.size
        val containerType = VirtualContainerType.getType(type, size) ?: return null
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
                baseComponentService.wrap(virtualContainer.title).getUnwrappedInstance()
            )
        )
    }

    private fun createAnvil(virtualContainer: VirtualAnvilContainer): VirtualMessage? {
        val container = containerService.wrap(virtualContainer.player)

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
                baseComponentService.wrap(ComponentSerializer.toString(virtualContainer.title)).getUnwrappedInstance()
            )
        )
    }
}