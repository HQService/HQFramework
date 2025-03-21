package kr.hqservice.framework.database.repository.player.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitAsync
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.lock.DefermentLock
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

@Listener
class PlayerConnectionPacketHandler(
    private val plugin: Plugin,
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val coroutineScope: CoroutineScope,
    @Qualifier("switch") private val switchDefermentLock: DefermentLock,
    @Qualifier("disconnect") private val disconnectDefermentLock: DefermentLock,
    private val server: Server,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
) {
    private var lockedPlayer = ConcurrentLinkedDeque<UUID>()

    @Subscribe(HandleOrder.FIRST)
    fun pickup(event: PlayerPickupItemEvent) {
        if (lockedPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: InventoryClickEvent) {
        if (lockedPlayer.contains(event.whoClicked.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerDropItemEvent) {
        if (lockedPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerCommandPreprocessEvent) {
        if (lockedPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerMoveEvent) {
        if (lockedPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

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
        return coroutineScope.launch(Dispatchers.IO + CoroutineName("PlayerRepositorySaveRoutine")) {
            playerRepositoryRegistry.getAll().forEach { repository ->
                onSave(player, repository)
                repository.remove(player.uniqueId)
            }
            //saveJobs.joinAll()
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
    @Subscribe
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

    @Subscribe
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
    @Subscribe
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (nettyService.isEnable()) {
            return
        }
        saveAndClear(event.player)
    }

    @Subscribe
    suspend fun onLoad(event: PlayerDataPreLoadEvent) {
        coroutineScope.launch(Dispatchers.BukkitAsync) {
            var cancelled = false
            lockedPlayer.add(event.player.uniqueId)
            withContext(Dispatchers.BukkitMain) {
                //event.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, -1, 10), true)
                server.onlinePlayers.forEach { it.hidePlayer(plugin, event.player) }
            }

            event.player.isInvulnerable = true
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
                event.player.isInvulnerable = false
                lockedPlayer.remove(event.player.uniqueId)
                return@launch
            }

            playerRepositoryRegistry.getAll().forEach { repository ->
                withContext(Dispatchers.IO) {
                    onLoad(event.player, repository)
                }
            }
            //loadJobs.joinAll()

            withContext(Dispatchers.BukkitMain) {
                server.onlinePlayers.forEach { it.showPlayer(plugin, event.player) }
                //event.player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 10, 10), true)
            }

            lockedPlayer.remove(event.player.uniqueId)
            pluginManager.callEvent(PlayerRepositoryLoadedEvent(event.player))

            bukkitDelay(30)
            event.player.isInvulnerable = false
        }
    }

    @Subscribe
    fun unlockWhenSaved(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet is PlayerDataSavedPacket) {
            ensureLoad(packet.player.getUniqueId())
        }
    }
}