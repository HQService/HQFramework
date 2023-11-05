package kr.hqservice.framework.nms.virtual.container

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.ContainerWrapper
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

open class VirtualContainer(
    private val player: Player,
    private val title: String
) : Virtual, KoinComponent {

    protected val reflectionWrapper: NmsReflectionWrapper by inject()
    protected val baseComponentService: NmsService<String, BaseComponentWrapper> by inject(named("base-component"))
    protected val containerService: NmsService<Player, ContainerWrapper> by inject(named("container"))

    protected val containersClass = reflectionWrapper.getNmsClass("Containers",
        Version.V_17.handle("world.inventory")
    )
    protected val packetClass = reflectionWrapper.getNmsClass("PacketPlayOutOpenWindow",
        Version.V_17.handle("network.protocol.game")
    )
    private val bukkitViewFunction = reflectionWrapper.getFunction(containerService.getTargetClass(), "getBukkitView")

    override fun createVirtualMessage(): VirtualMessage? {
        val container = containerService.wrap(player)

        val bukkitView = bukkitViewFunction.call(container.getUnwrappedInstance())
        if (bukkitView !is InventoryView) throw UnsupportedOperationException("error1")

        val type = bukkitView.topInventory.type
        val size = bukkitView.topInventory.size
        val containerType = VirtualContainerType.getType(type, size) ?: return null
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