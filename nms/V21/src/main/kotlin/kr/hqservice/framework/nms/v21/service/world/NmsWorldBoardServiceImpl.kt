package kr.hqservice.framework.nms.v21.service.world

import kr.hqservice.framework.nms.service.world.NmsWorldBorderService
import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.world.WorldBorderWrapper
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder
import org.bukkit.World
import kotlin.reflect.KClass

class NmsWorldBoardServiceImpl(
    private val worldService: NmsWorldService
) : NmsWorldBorderService {
    override fun createCenterPacket(x: Double, z: Double, wrapper: WorldBorderWrapper): Any {
        val border = wrapper.getUnwrappedInstance() as WorldBorder
        border.setCenter(x, z)
        return ClientboundSetBorderCenterPacket(border)
    }

    override fun createWorldBorderWarningBlocksPacket(distance: Int, wrapper: WorldBorderWrapper): Any {
        val border = wrapper.getUnwrappedInstance() as WorldBorder
        border.warningTime = distance
        return border
    }

    override fun setSize(size: Double, wrapper: WorldBorderWrapper) {
        val border = wrapper.getUnwrappedInstance() as WorldBorder
        border.size = size
    }

    override fun createVirtualMessage(border: VirtualWorldBorder, wrapper: WorldBorderWrapper): VirtualMessage {
        val nmsBorder = wrapper.getUnwrappedInstance() as WorldBorder
        val packet = ClientboundInitializeBorderPacket(nmsBorder)
        return if (border.packetQueue.isNotEmpty()) {
            val result = VirtualMessageImpl(packet)
            border.packetQueue.clear()
            result
        } else VirtualMessageImpl(packet)
    }

    override fun wrap(target: World): WorldBorderWrapper {
        val worldBorderInst = WorldBorder()
        val worldWrapper = worldService.wrap(target)
        worldBorderInst.world = worldWrapper.getUnwrappedInstance() as ServerLevel
        return WorldBorderWrapper(worldBorderInst)
    }

    override fun unwrap(wrapper: WorldBorderWrapper): World {
        throw UnsupportedOperationException()
    }

    override fun getOriginalClass(): KClass<*> {
        return World::class
    }

    override fun getTargetClass(): KClass<*> {
        return WorldBorder::class
    }
}