package kr.hqservice.framework.nms.virtual.world

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.setAccess
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualWorldBorder(
    world: World
) : Virtual, KoinComponent {
    private val reflectionWrapper: NmsReflectionWrapper by inject()
    private val service: NmsService<World, WorldBorderWrapper> by inject(named("nms.world.border"))
    private val packetClass =
        reflectionWrapper.getNmsClass("ClientboundInitializeBorderPacket", Version.V_15.handle("network.protocol.game"))
    private val borderWrapper = service.wrap(world)
    private val extendClass = reflectionWrapper.getNmsClass(
        "WorldBorder\$d",
        Version.V_15.handle("world.level.border")
    )
    private val extendConstructor = extendClass.java.getConstructor(service.getTargetClass().java, Double::class.javaPrimitiveType).apply {
        isAccessible = true
    }

    private val xField = reflectionWrapper.getField(service.getTargetClass(), "h",
        Version.V_19.handle("i"))
    private val zField = reflectionWrapper.getField(service.getTargetClass(), "i",
        Version.V_19.handle("j"))
    private val sizeField = reflectionWrapper.getField(service.getTargetClass(), "l",
        Version.V_19.handle("l"))
    private var centerPacket: Any? = null

    fun setCenter(x: Double, z: Double) {
        centerPacket = WorldBorderCenter(x, z).createPacket()
    }

    fun setSize(size: Double) {
        sizeField.setAccess(
            borderWrapper.getUnwrappedInstance(),
            extendConstructor.newInstance(borderWrapper.getUnwrappedInstance(), size)
        )
    }

    override fun createVirtualMessage(): VirtualMessage {
        val constructor = packetClass.java.getConstructor(service.getTargetClass().java)
        val packetInst = constructor.newInstance(borderWrapper.getUnwrappedInstance())
        return if (centerPacket != null) {
            val result = VirtualListMessage(listOf(packetInst, centerPacket!!))
            centerPacket = null
            result
        } else VirtualMessageImpl(packetInst)
    }

    private inner class WorldBorderCenter(
        private val x: Double,
        private val z: Double
    ) {
        private val packetClass =
            reflectionWrapper.getNmsClass("ClientboundSetBorderCenterPacket", Version.V_15.handle("network.protocol.game"))
        private val constructor = packetClass.java.getConstructor(service.getTargetClass().java)

        fun createPacket(): Any {
            xField.setAccess(borderWrapper.getUnwrappedInstance(), x)
            zField.setAccess(borderWrapper.getUnwrappedInstance(), z)
            return constructor.newInstance(borderWrapper.getUnwrappedInstance())
        }
    }
}