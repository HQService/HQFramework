package kr.hqservice.framework.velocity.multi.netty.api

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import com.velocitypowered.api.proxy.ProxyServer
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.velocity.core.netty.api.ProxyNettyServer

@Bean
class RedisProxyNettyServer(
    proxy: ProxyServer
) : ProxyNettyServer(proxy) {
    private val redis = RedisBungeeAPI.getRedisBungeeApi()
    private val absRedis = RedisBungeeAPI.getAbstractRedisBungeeAPI()

    override fun getPlayers(): List<NettyPlayer> {
        return redis.serverToPlayers.asMap().map { entry ->
            entry.value.map {
                val playerName = absRedis.getNameFromUuid(it)
                NettyPlayerImpl(playerName, playerName, it, getChannel(entry.key))
            }
        }.flatten()
    }
}
