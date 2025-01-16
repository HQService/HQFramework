package kr.hqservice.framework.nms.v21.virtual.entity

import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.v21.service.world.NmsWorldServiceImpl
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.SignBlockEntity
import org.bukkit.entity.Player

class VirtualSignFactoryImpl(
    private val worldService: NmsWorldService
) : VirtualEntityFactory {
    override fun create(
        player: Player,
        virtualEntity: AbstractVirtualEntity?,
    ): VirtualMessage {
        worldService as NmsWorldServiceImpl
        val worldServer = worldService.wrap(player.world)
        val blockPosition = worldServer.getBlockPosition(player.location)
        val iBlockData = worldServer.getIBlockData(blockPosition)
        val tileEntity = BlockEntityType.SIGN.create(blockPosition, iBlockData)
        val tileEntitySign = tileEntity as SignBlockEntity
        val tileEntityDataPacket = ClientboundBlockEntityDataPacket.create(tileEntitySign)
        val signEditorDataPacket = ClientboundOpenSignEditorPacket(blockPosition, true)
        return VirtualListMessage(listOf(tileEntityDataPacket, signEditorDataPacket))
    }
}