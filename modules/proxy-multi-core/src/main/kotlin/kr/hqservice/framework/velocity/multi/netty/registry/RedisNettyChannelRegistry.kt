package kr.hqservice.framework.velocity.multi.netty.registry

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.netty.api.NettyChannel
import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.api.impl.NettyPlayerImpl
import kr.hqservice.framework.velocity.core.HQVelocityPlugin
import kr.hqservice.framework.velocity.core.netty.registry.NettyChannelRegistry
import kr.hqservice.framework.velocity.core.netty.registry.impl.NettyChannelRegistryImpl
import kr.hqservice.framework.yaml.config.HQYamlConfiguration

@Component
@Singleton(binds = [NettyChannelRegistry::class])
class RedisNettyChannelRegistry(
    plugin: HQVelocityPlugin,
    config: HQYamlConfiguration
) : NettyChannelRegistryImpl(plugin, config) {
    override fun collectPlayers(connectedChannels: List<NettyChannel>): MutableList<NettyPlayer> {
        val players = mutableListOf<NettyPlayer>()
        val redisAbs = RedisBungeeAPI.getAbstractRedisBungeeAPI()
        runCatching {
            redisAbs.serverToPlayers.entries().forEach {
                try {
                    players.add(
                        NettyPlayerImpl(
                            redisAbs.getNameFromUuid(it.value),
                            redisAbs.getNameFromUuid(it.value),
                            it.value,
                            connectedChannels.firstOrNull { channel -> channel.getName() == it.key }
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return players
    }
}
