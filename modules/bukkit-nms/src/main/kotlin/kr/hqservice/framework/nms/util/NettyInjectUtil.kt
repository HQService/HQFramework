package kr.hqservice.framework.nms.util

import io.netty.channel.Channel
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.UUID

interface NettyInjectUtil {
    fun getPlayerChannel(player: Player): Channel

    @Deprecated("")
    fun getServerChannels(server: Server): List<Channel>

    @Deprecated("")
    fun injectHandler(player: Player)

    fun injectHandler(uniqueId: UUID, userAddress: String)

    fun removeHandler(player: Player)
}