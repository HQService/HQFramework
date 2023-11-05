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
    private val borderWrapper = service.wrap(world)

    private val packetClass = reflectionWrapper.getNmsClass("ClientboundInitializeBorderPacket",
        Version.V_17.handle("network.protocol.game")
    )
    private val extentField = reflectionWrapper.getField(service.getTargetClass(), "extent",
        Version.V_17.handle("k"),
        Version.V_18.handle("l"),
        Version.V_17_FORGE.handle("f_61914_")
    )
    private val extentClass = reflectionWrapper.getNmsClass("WorldBorder\$d",
        Version.V_17.handle("world.level.border")
    )
    private val extentConstructor = extentClass.java.getConstructor(service.getTargetClass().java, Double::class.javaPrimitiveType).apply {
        isAccessible = true
    }

    private var packetQueue = mutableMapOf<Int, Any>()

    fun setCenter(x: Double, z: Double) {
        packetQueue[1] = WorldBorderCenter(x, z).createPacket()
    }

    fun setWarningDistance(distance: Int) {
        packetQueue[2] = WorldBorderWarningBlocks(distance).createPacket()
    }

    fun setSize(size: Double) {
        extentField.setAccess(
            borderWrapper.getUnwrappedInstance(),
            extentConstructor.newInstance(borderWrapper.getUnwrappedInstance(), size)
        )
    }

    override fun createVirtualMessage(): VirtualMessage {
        val constructor = packetClass.java.getConstructor(service.getTargetClass().java)
        val packetInst = constructor.newInstance(borderWrapper.getUnwrappedInstance())
        return if (packetQueue.isNotEmpty()) {
            val result = VirtualListMessage(listOf(packetInst) + packetQueue.values)
            packetQueue.clear()
            result
        } else VirtualMessageImpl(packetInst)
    }

    private inner class WorldBorderCenter(
        private val x: Double,
        private val z: Double
    ) {

        private val packetClass = reflectionWrapper.getNmsClass("ClientboundSetBorderCenterPacket",
            Version.V_17.handle("network.protocol.game")
        )
        private val constructor = packetClass.java.getConstructor(service.getTargetClass().java)

        private val centerXField = reflectionWrapper.getField(
            service.getTargetClass(), "centerX",
            Version.V_17.handle("h"),
            Version.V_18.handle("i"),
            Version.V_17_FORGE.handle("f_61911_")
        )
        private val centerZField = reflectionWrapper.getField(
            service.getTargetClass(), "centerZ",
            Version.V_17.handle("i"),
            Version.V_18.handle("j"),
            Version.V_17_FORGE.handle("f_61912_")
        )

        fun createPacket(): Any {
            centerXField.setAccess(borderWrapper.getUnwrappedInstance(), x)
            centerZField.setAccess(borderWrapper.getUnwrappedInstance(), z)
            return constructor.newInstance(borderWrapper.getUnwrappedInstance())
        }
    }

    private inner class WorldBorderWarningBlocks(
        private val blocks: Int
    ) {

        private val packetClass = reflectionWrapper.getNmsClass(
            "ClientboundSetBorderWarningDistancePacket", Version.V_17.handle("network.protocol.game")
        )
        private val constructor = packetClass.java.getConstructor(service.getTargetClass().java)

        private val warningBlocksField = reflectionWrapper.getField(
            service.getTargetClass(), "warningBlocks",
            Version.V_17.handle("g"),
            Version.V_18.handle("h"),
            Version.V_17_FORGE.handle("f_61910_")
        )

        fun createPacket(): Any {
            warningBlocksField.setAccess(borderWrapper.getUnwrappedInstance(), blocks)
            return constructor.newInstance(borderWrapper.getUnwrappedInstance())
        }
    }
}