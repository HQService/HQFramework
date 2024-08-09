package kr.hqservice.framework.nms.virtual.entity.inner

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.VirtualMessage
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.wrapper.world.WorldWrapper
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.SignBlockEntity
import org.bukkit.World
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class VirtualSign(
    private val player: Player,
) : Virtual, KoinComponent {
    private val worldService: NmsService<World, WorldWrapper> by inject(named("nms.world"))
    override fun createVirtualMessage(): VirtualMessage {
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