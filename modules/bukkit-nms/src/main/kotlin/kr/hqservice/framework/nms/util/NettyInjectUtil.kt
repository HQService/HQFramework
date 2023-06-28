package kr.hqservice.framework.nms.util

import io.netty.channel.Channel
import org.bukkit.Server
import org.bukkit.entity.Player

interface NettyInjectUtil {

    fun getPlayerChannel(player: Player): Channel

    fun getServerChannels(server: Server): List<Channel>

    fun injectHandler(player: Player)

    fun removeHandler(player: Player)

}