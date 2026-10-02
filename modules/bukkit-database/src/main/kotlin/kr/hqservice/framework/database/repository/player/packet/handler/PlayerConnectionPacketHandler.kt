package kr.hqservice.framework.database.repository.player.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.bukkit.core.listener.HandleOrder
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import kr.hqservice.framework.bukkit.core.netty.event.AsyncNettyPacketReceivedEvent
import kr.hqservice.framework.bukkit.core.netty.service.HQNettyService
import kr.hqservice.framework.database.repository.player.PlayerRepository
import kr.hqservice.framework.database.repository.player.event.PlayerRepositoryLoadedEvent
import kr.hqservice.framework.database.repository.player.lock.DefermentLock
import kr.hqservice.framework.database.repository.player.lock.SwitchGate
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavePacket
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

@Listener
class PlayerConnectionPacketHandler(
    private val plugin: Plugin,
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val coroutineScope: CoroutineScope,
    @Qualifier("switch") private val switchDefermentLock: DefermentLock,
    private val switchGate: SwitchGate,
    private val server: Server,
    private val pluginManager: PluginManager,
    private val packetSender: PacketSender,
    private val nettyService: HQNettyService,
) {
    private var loadPlayer = ConcurrentHashMap.newKeySet<UUID>()
    private val playerScopes = PlayerScopes(coroutineScope, Dispatchers.IO)

    @Subscribe(HandleOrder.FIRST)
    fun pickup(event: PlayerPickupItemEvent) {
        if (loadPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: InventoryClickEvent) {
        if (loadPlayer.contains(event.whoClicked.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerDropItemEvent) {
        if (loadPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerCommandPreprocessEvent) {
        if (loadPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    @Subscribe(HandleOrder.FIRST)
    fun onClick(event: PlayerMoveEvent) {
        if (loadPlayer.contains(event.player.uniqueId))
            event.isCancelled = true
    }

    private suspend fun <T : Any> onLoad(player: Player, repository: PlayerRepository<T>) {
        val value = repository.load(player)
        if (player.isOnline) repository[player.uniqueId] = value else repository.remove(player.uniqueId)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun onSave(player: Player, repository: PlayerRepository<*>, snapshot: Any?) {
        val value = snapshot ?: return
        repository as PlayerRepository<Any>
        repository.save(player, value)
    }

    private suspend fun saveAndClear(player: Player) = coroutineScope {
        val playerRepositories = playerRepositoryRegistry.getAll()
        val values = playerRepositories.associateWith { it[player.uniqueId] }
        runCatching {
            newSuspendedTransaction(Dispatchers.IO + CoroutineName("save:${player.uniqueId}")) {
                for (repo in playerRepositories) {
                    withContext(CoroutineName("save:${player.uniqueId}:${repo::class.simpleName}")) {
                        onSave(player, repo, values[repo])
                    }
                }
            }

            for (repo in playerRepositories) {
                removeIfUnchanged(repo, player.uniqueId, values[repo])
            }
        }.onFailure {
            if (it is CancellationException) throw it
            plugin.logger.log(Level.SEVERE, "failed to save player data for ${player.uniqueId}", it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun removeIfUnchanged(repository: PlayerRepository<*>, id: UUID, snapshot: Any?) {
        if (snapshot != null) (repository as PlayerRepository<Any>).remove(id, snapshot)
    }

    // proxied server
    @Subscribe
    fun onPacketReceive(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet !is PlayerConnectionPacket) {
            return
        }

        if (packet.state == PlayerConnectionState.CONNECTED) {
            unlock(packet.player.getUniqueId())
            switchGate.signal(packet.player.getUniqueId())
            return
        }
    }

    @Subscribe
    fun onJoin(event: PlayerJoinEvent) {
        plugin as HQBukkitPlugin
        val player = event.player
        val playerId = player.uniqueId
        plugin.launch(Dispatchers.Default) {
            loadPlayer.add(playerId)
            try {
                playerScopes.awaitIdle(playerId)
                delay(50)

                if (nettyService.isEnable()) {
                    val lock = switchDefermentLock.findLock(playerId)
                    if (lock != null && !lock.isCancelled && lock.isActive) {
                        val ok = withTimeoutOrNull(3000) { switchGate.ensure(playerId).await() } != null
                        if (!ok) {
                            withContext(Dispatchers.BukkitMain) {
                                player.kickPlayer("데이터 저장 시점을 받아오지 못하였습니다.")
                            }
                            switchGate.release(playerId)
                            return@launch
                        } else delay(1)
                    }
                    switchGate.release(playerId)
                }

                delay(5)
                var exactLoadedCount = -1
                var loaded = 0
                val tryLoad = suspend {
                    loaded = 0
                    server.getPlayer(playerId)?.let { currentPlayer ->
                        val repositories = playerRepositoryRegistry.getAll()
                        try {
                            newSuspendedTransaction(Dispatchers.IO + CoroutineName("load:${playerId}")) {
                                for (repo in repositories) {
                                    onLoad(currentPlayer, repo)
                                    loaded++
                                }
                            }
                            exactLoadedCount = repositories.size
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            plugin.logger.log(Level.WARNING, "Failed to load player data for $playerId", e)
                        }
                    }
                }
                tryLoad.invoke()
                delay(5)

                if (exactLoadedCount == -1) {
                    plugin.logger.warning("Failed to load player data for $playerId, retrying once...")
                    delay(250)
                    tryLoad.invoke()
                }

                if (exactLoadedCount == loaded) {
                    server.getPlayer(playerId)?.let { pluginManager.callEvent(PlayerRepositoryLoadedEvent(it)) }
                } else {
                    server.getPlayer(playerId)?.let { withContext(Dispatchers.BukkitMain) { it.kick() } }
                }
            } finally {
                loadPlayer.remove(playerId)
            }
        }
    }

    @Subscribe
    fun onPlayerProxyQuit(event: PlayerQuitEvent) {
        if (!nettyService.isEnable()) return
        val player = event.player
        packetSender.sendPacketAll(PlayerDataSavePacket(player.uniqueId))

        playerScopes.scope(player.uniqueId).launch(TeardownOptionCoroutineContextElement(false)) {
            saveAndClear(player)
            packetSender.sendPacketAll(PlayerDataSavedPacket(player.uniqueId))
        }.invokeOnCompletion { playerScopes.releaseIfIdle(player.uniqueId) }
    }

    // non proxied server
    @Subscribe
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (nettyService.isEnable()) return
        playerScopes.scope(event.player.uniqueId).launch(CoroutineName("save")) {
            saveAndClear(event.player)
        }.invokeOnCompletion { playerScopes.releaseIfIdle(event.player.uniqueId) }
    }

    @Subscribe
    fun playerDataPacketHandle(event: AsyncNettyPacketReceivedEvent) {
        when (val packet = event.packet) {
            is PlayerDataSavePacket -> {
                switchGate.reset(packet.id)
                playerScopes.scope(packet.id).launch { lock(packet.id) }
            }
            is PlayerDataSavedPacket -> {
                switchGate.signal(packet.id)
                playerScopes.scope(packet.id).launch { unlock(packet.id) }
            }
        }
    }

    private suspend fun lock(uniqueId: UUID) {
        val lock = switchDefermentLock.findLock(uniqueId)
        if (lock == null) {
            switchDefermentLock.tryLock(uniqueId) {
                withContext(Dispatchers.BukkitMain) {
                    server.getPlayer(uniqueId)?.kickPlayer("데이터 저장 시점을 받아오지 못하였습니다.")
                }
            }
        }
    }

    private fun unlock(uniqueId: UUID) {
        val lock = switchDefermentLock.findLock(uniqueId)
        if (lock != null) {
            switchDefermentLock.unlock(uniqueId)
        }
    }
}