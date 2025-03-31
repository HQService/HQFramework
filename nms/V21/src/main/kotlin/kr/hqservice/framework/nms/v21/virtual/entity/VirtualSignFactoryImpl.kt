package kr.hqservice.framework.nms.v21.virtual.entity

import kr.hqservice.framework.nms.service.world.NmsWorldService
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.SignBlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.entity.Player

class VirtualSignFactoryImpl(
    private val worldService: NmsWorldService
) : VirtualEntityFactory {
    override fun create(
        player: Player,
        virtualEntity: AbstractVirtualEntity?,
    ): VirtualMessage {
        val blockPos = BlockPos(player.location.blockX, player.location.blockY, player.location.blockZ)
        val fakeSignBlockState: BlockState = Blocks.OAK_SIGN.defaultBlockState()
        val virtualSignEntity = SignBlockEntity(blockPos, fakeSignBlockState)
        virtualSignEntity.setLevel(worldService.wrap(player.world).getUnwrappedInstance() as ServerLevel)

        return VirtualListMessage(listOf(
            ClientboundBlockUpdatePacket(blockPos, fakeSignBlockState),
            ClientboundBlockEntityDataPacket.create(virtualSignEntity),
            ClientboundOpenSignEditorPacket(blockPos, true)
        ))
    }
}