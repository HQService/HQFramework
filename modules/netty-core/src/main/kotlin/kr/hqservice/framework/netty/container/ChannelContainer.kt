package kr.hqservice.framework.netty.container

import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer

interface ChannelContainer {
    fun getChannels(): List<NettyChannel>

    fun getPlayers(): List<NettyPlayer>

    fun getPlayers(channel: NettyChannel): List<NettyPlayer>
}