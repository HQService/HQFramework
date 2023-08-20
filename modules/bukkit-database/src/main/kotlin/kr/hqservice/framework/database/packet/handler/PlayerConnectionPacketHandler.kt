package kr.hqservice.framework.database.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.database.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.lock.DefermentLock
import kr.hqservice.framework.database.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.database.repository.PlayerRepository
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

@Component
class PlayerConnectionPacketHandler(
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val coroutineScope: CoroutineScope,
    @Qualifier("database") private val databaseCoroutineScope: CoroutineScope,
    @Qualifier("switch") private val switchDefermentLock: DefermentLock,
    @Qualifier("disconnect") private val disconnectDefermentLock: DisconnectDefermentLock,
    private val server: Server,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
    @Qualifier("switch") private val switchDefermentLock: DefermentLock,
    @Qualifier("disconnect") private val disconnectDefermentLock: DefermentLock,
) : HQListener {
    private suspend fun <T : Any> onLoad(player: Player, repository: PlayerRepository<T>) {
        val value = newSuspendedTransaction(coroutineScope.coroutineContext) {
            repository.load(player)
        }
        if (player.isOnline) {
            repository[player.uniqueId] = value
        }
    }

    private suspend fun <T : Any> onSave(player: Player, repository: PlayerRepository<T>) {
        val value = repository[player.uniqueId] ?: return
        newSuspendedTransaction(coroutineScope.coroutineContext) {
            repository.save(player, value)
        }
    }

    private fun saveAndClear(player: Player): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val saveJobs = playerRepositoryRegistry.getAll().map { repository ->
                launch {
                    onSave(player, repository)
                    repository.remove(player.uniqueId)
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
            coroutineScope.launch(Dispatchers.IO) {
                switchDefermentLock.tryLock(playerId) {
                    launch(Dispatchers.BukkitMain){
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
                val player =
                    server.getPlayer(packet.player.getUniqueId()) ?: throw NullPointerException("player not found")
                val nextChannel = packet.sourceChannel ?: return // 열리지 않은 서버
                coroutineScope.launch(Dispatchers.IO) {
                    saveAndClear(player).join()
                    packetSender.sendPacket(nextChannel.getPort(), PlayerDataSavedPacket(packet.player))
                }
            }

            PlayerConnectionState.DISCONNECT -> {
                val lock = disconnectDefermentLock.findLock(packet.player.getUniqueId())
                if (lock == null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        disconnectDefermentLock.tryLock(packet.player.getUniqueId(), 1000L) {}
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
        coroutineScope.launch(Dispatchers.IO) {
            val lock = disconnectDefermentLock.findLock(event.player.uniqueId)
            if (lock != null) {
                saveAndClear(event.player)
            } else {
                var timedOut = false
                disconnectDefermentLock.tryLock(event.player, 1000L) {
                    timedOut = true
                }
                if (!timedOut) {
                    saveAndClear(event.player)
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
        saveAndClear(event.player)
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
                    launch(Dispatchers.BukkitMain) {
                        player.kickPlayer("데이터 저장 시점을 받아오지 못하였습니다.")
                        cancelled = true
                    }.join()
                }.join()
            }
        }
        if (cancelled) {
            return@blocking
        }
        val loadJobs = playerRepositoryRegistry.getAll().map { repository ->
            coroutineScope.launch(Dispatchers.IO) {
                onLoad(event.player, repository)
            }
        }
        loadJobs.joinAll()
        pluginManager.callEvent(PlayerRepositoryLoadedEvent(event.player))
    }

    @EventHandler
    fun unlockWhenSaved(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet is PlayerDataSavedPacket) {
            ensureLoad(packet.player.getUniqueId())
        }
    }
}