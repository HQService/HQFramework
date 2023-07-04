package kr.hqservice.framework.database.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.coroutine.DefermentCoroutineScope
import kr.hqservice.framework.database.coroutine.DefermentLock
import kr.hqservice.framework.database.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.extension.print
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
    private val defermentCoroutineScope: DefermentCoroutineScope,
    private val defermentLock: DefermentLock,
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
        val lock = defermentLock.findLock(playerId)
        if (lock != null) {
            defermentLock.unlock(playerId)
        } else {
            defermentCoroutineScope.launch {
                defermentLock.tryLock(playerId) {
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
                println("preswitched: ${packet.player}, ${packet.sourceChannel?.getPort()}")
                val player = server.getPlayer(packet.player.getUniqueId()) ?: throw NullPointerException("player not found")
                defermentCoroutineScope.launch {
                    saveAll(player).join()
                    val nextChannel = packet.sourceChannel ?: throw NullPointerException("null 이면 안되는데...")
                    packetSender.sendPacket(nextChannel.getPort().print("sendto: "), PlayerDataSavedPacket(packet.player))
                    println("packet send")
                }
            }
            PlayerConnectionState.DISCONNECT -> {
//                val player = server.getPlayer(packet.player.getUniqueId()) ?: throw NullPointerException("player not found")
//                saveAll(player)
            }

            else -> {}
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
            val lock = defermentLock.findLock(event.player.uniqueId)
            if (lock != null) {
                defermentLock.unlock(event.player.uniqueId)
            } else {
                defermentLock.tryLock(event.player) whenTimedOut@{ player ->
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