package kr.hqservice.framework.database.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.lock.DefermentLock
import kr.hqservice.framework.database.lock.impl.DisconnectDefermentLock
import kr.hqservice.framework.database.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginManager
import org.koin.core.annotation.Named
import java.util.*

@Component
class PlayerConnectionPacketHandler(
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    @Named("main") private val mainCoroutineScope: CoroutineScope,
    @Named("database") private val databaseCoroutineScope: CoroutineScope,
    @Named("switch") private val switchDefermentLock: DefermentLock,
    @Named("disconnect") private val disconnectDefermentLock: DisconnectDefermentLock,
    private val server: Server,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
) : HQListener {
    private fun saveAll(player: Player): Job {
        return databaseCoroutineScope.launch {
            val saveJobs = playerRepositoryRegistry.getAll().map {
                launch {
                    it.onSave(player)
                }
            }
            saveJobs.joinAll()
        }
    }

    private fun ensureLoad(playerId: UUID) {
        val lock = switchDefermentLock.findLock(playerId)
        if (lock != null) {
            switchDefermentLock.unlock(playerId)
        } else {
            databaseCoroutineScope.launch {
                switchDefermentLock.tryLock(playerId) {
                    mainCoroutineScope.launch {
                        server.getPlayer(it)?.kickPlayer("데이터 로드 시점을 받아오지 못하였습니다.")
                        cancel()
                    }
                }
            }
        }
    }

    // proxied server
    @EventHandler
    fun onPacketReceive(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet !is PlayerConnectionPacket) {
            return
        }

        if (packet.state == PlayerConnectionState.CONNECTED && packet.sourceChannel?.getPort() == server.port) {
            ensureLoad(packet.player.getUniqueId())
            return
        }
        if (packet.player.getChannel()?.getPort() != server.port) {
            return
        }
        when (packet.state) {
            PlayerConnectionState.PRE_SWITCH_CHANNEL -> {
                val player = server.getPlayer(packet.player.getUniqueId()) ?: throw NullPointerException("player not found")
                databaseCoroutineScope.launch {
                    saveAll(player).join()
                    val nextChannel = packet.sourceChannel ?: throw NullPointerException("null 이면 안되는데...")
                    packetSender.sendPacket(nextChannel.getPort(), PlayerDataSavedPacket(packet.player))
                }
            }
            PlayerConnectionState.DISCONNECT -> {
                val lock = disconnectDefermentLock.findLock(packet.player.getUniqueId())
                if (lock == null) {
                    databaseCoroutineScope.launch {
                        disconnectDefermentLock.tryLock(packet.player.getUniqueId(), 500L) {}
                    }
                } else {
                    disconnectDefermentLock.unlock(packet.player.getUniqueId())
                }
            }

            else -> {}
        }
    }
    @EventHandler
    fun onPlayerProxyQuit(event: PlayerQuitEvent) {
        if (!nettyService.isEnable()) {
            return
        }
        databaseCoroutineScope.launch {
            val lock = disconnectDefermentLock.findLock(event.player.uniqueId)
            if (lock != null) {
                saveAll(event.player)
            } else {
                var timedOut = false
                disconnectDefermentLock.tryLock(event.player, 500L) {
                    timedOut = true
                }
                if (!timedOut) {
                    saveAll(event.player)
                }
            }
        }
    }

    // non proxied server
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (nettyService.isEnable()) {
            return
        }
        saveAll(event.player)
    }

    @EventHandler
    fun onLoad(event: PlayerDataPreLoadEvent): Unit = runBlocking blocking@{
        var cancelled = false
        if (nettyService.isEnable()) {
            val lock = switchDefermentLock.findLock(event.player.uniqueId)
            if (lock != null) {
                switchDefermentLock.unlock(event.player.uniqueId)
            } else {
                switchDefermentLock.tryLock(event.player) whenTimedOut@{ player ->
                    mainCoroutineScope.launch {
                        player.kickPlayer("데이터 저장 시점을 받아오지 못하였습니다.")
                        cancelled = true
                    }.join()
                }.join()
            }
        }
        if (cancelled) {
            return@blocking
        }
        val loadJobs = playerRepositoryRegistry.getAll().map {
            databaseCoroutineScope.launch {
                it.onLoad(event.player)
            }
        }
        loadJobs.joinAll()
        pluginManager.callEvent(PlayerRepositoryLoadedEvent(event.player))
    }

    @EventHandler
    fun unlockWhenSaved(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet is PlayerDataSavedPacket) {
            println("player data saved packet received")
            ensureLoad(packet.player.getUniqueId())
        }
    }
}