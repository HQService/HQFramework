package kr.hqservice.framework.bungee.core.netty.registry

import kr.hqservice.framework.netty.channel.ChannelWrapper

interface NettyChannelRegistry {
    fun registerActiveChannel(port: Int, wrapper: ChannelWrapper)

    fun loopChannels(block: (ChannelWrapper) -> Unit)

    fun getChannels(): List<ChannelWrapper>

    fun getChannelNameByPort(port: Int): String

    fun getChannelByPort(port: Int): ChannelWrapper

    fun getChannelByServerName(name: String): ChannelWrapper

    fun forEachChannels(block: (ChannelWrapper) -> Unit)

    fun shutdown()
}