package kr.hqservice.framework.nms.legacy.service.world

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.extension.setAccess
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.service.world.WorldBorderService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import org.bukkit.World
import kotlin.reflect.KClass

@Qualifier("nms.world.border")
@Service
class LegacyWorldBorderService(
    private val reflectionWrapper: NmsReflectionWrapper,
    @Qualifier("nms.world") private val worldService: NmsService<World, WorldWrapper>
) : WorldBorderService {

    private val worldBorderClass = reflectionWrapper.getNmsClass("WorldBorder",
        Version.V_17.handle("world.level.border")
    )
    private val worldBorderConstructor = worldBorderClass.constructors.first { it.parameters.isEmpty() }
    private val worldField = worldBorderClass.java.getField("world")

    private val packetClass = reflectionWrapper.getNmsClass("ClientboundInitializeBorderPacket",
        Version.V_17.handle("network.protocol.game")
    )
    private val extentField = reflectionWrapper.getField(getTargetClass(), "extent",
        Version.V_17.handle("k"),
        Version.V_18.handle("l"),
        Version.V_17_FORGE.handle("f_61914_")
    )
    private val extentClass = reflectionWrapper.getNmsClass("WorldBorder\$d",
        Version.V_17.handle("world.level.border")
    )
    private val extentConstructor = extentClass.java.getConstructor(getTargetClass().java, Double::class.javaPrimitiveType).apply {
        isAccessible = true
    }

    override fun createCenterPacket(x: Double, z: Double, wrapper: WorldBorderWrapper): Any {
        return WorldBorderCenter(x, z).createPacket(wrapper)
    }

    override fun createWorldBorderWarningBlocksPacket(distance: Int, wrapper: WorldBorderWrapper): Any {
        return WorldBorderWarningBlocks(distance).createPacket(wrapper)
    }

    override fun setSize(size: Double, wrapper: WorldBorderWrapper) {
        extentField.setAccess(
            wrapper.getUnwrappedInstance(),
            extentConstructor.newInstance(wrapper.getUnwrappedInstance(), size)
        )
    }

    override fun createVirtualMessage(border: VirtualWorldBorder, wrapper: WorldBorderWrapper): VirtualMessage {
        val constructor = packetClass.java.getConstructor(getTargetClass().java)
        val packetInst = constructor.newInstance(wrapper.getUnwrappedInstance())
        return if (border.packetQueue.isNotEmpty()) {
            val result = VirtualListMessage(listOf(packetInst) + border.packetQueue.values)
            border.packetQueue.clear()
            result
        } else VirtualMessageImpl(packetInst)
    }

    override fun wrap(target: World): WorldBorderWrapper {
        val worldBorderInst = worldBorderConstructor.call()
        val worldWrapper = worldService.wrap(target)
        worldField.set(worldBorderInst, worldWrapper.getUnwrappedInstance())
        return WorldBorderWrapper(worldBorderInst)
    }

    override fun unwrap(wrapper: WorldBorderWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return worldBorderClass
    }

    private inner class WorldBorderCenter(
        private val x: Double,
        private val z: Double,
    ) {
        private val packetClass = reflectionWrapper.getNmsClass("ClientboundSetBorderCenterPacket",
            Version.V_17.handle("network.protocol.game")
        )
        private val constructor = packetClass.java.getConstructor(getTargetClass().java)

        private val centerXField = reflectionWrapper.getField(
            getTargetClass(), "centerX",
            Version.V_17.handle("h"),
            Version.V_18.handle("i"),
            Version.V_17_FORGE.handle("f_61911_")
        )
        private val centerZField = reflectionWrapper.getField(
            getTargetClass(), "centerZ",
            Version.V_17.handle("i"),
            Version.V_18.handle("j"),
            Version.V_17_FORGE.handle("f_61912_")
        )

        fun createPacket(borderWrapper: WorldBorderWrapper): Any {
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
        private val constructor = packetClass.java.getConstructor(getTargetClass().java)

        private val warningBlocksField = reflectionWrapper.getField(
            getTargetClass(), "warningBlocks",
            Version.V_17.handle("g"),
            Version.V_18.handle("h"),
            Version.V_17_FORGE.handle("f_61910_")
        )

        fun createPacket(borderWrapper: WorldBorderWrapper): Any {
            warningBlocksField.setAccess(borderWrapper.getUnwrappedInstance(), blocks)
            return constructor.newInstance(borderWrapper.getUnwrappedInstance())
        }
    }
}