package kr.hqservice.framework.database.repository.player.packet.handler

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.PlayerScopes
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
import kr.hqservice.framework.database.repository.player.packet.PlayerDataSavedPacket
import kr.hqservice.framework.database.repository.player.registry.PlayerRepositoryRegistry
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.netty.api.PacketSender
import kr.hqservice.framework.netty.packet.player.PlayerConnectionPacket
import kr.hqservice.framework.netty.packet.player.PlayerConnectionState
import kr.hqservice.framework.nms.event.AsyncPlayerDataPreLoadEvent
import kr.hqservice.framework.nms.event.PlayerDataPreLoadEvent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Listener
class PlayerConnectionPacketHandler(
    private val plugin: Plugin,
    private val playerRepositoryRegistry: PlayerRepositoryRegistry,
    private val coroutineScope: CoroutineScope,
    @Qualifier("switch") private val switchDefermentLock: DefermentLock,
    @Qualifier("disconnect") private val disconnectDefermentLock: DefermentLock,
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

    @Subscribe(HandleOrder.FIRST)
    fun onPluginDisabled(event: PluginDisableEvent) {
        if (event.plugin.name == plugin.name) {
            playerScopes.cancelAll()
        }
    }

    private suspend fun <T : Any> onPreLoad(playerId: UUID, repository: PlayerRepository<T>) =
        newSuspendedTransaction(Dispatchers.IO) { repository.preLoad0(playerId) }

    private suspend fun <T : Any> onLoad(player: Player, repository: PlayerRepository<T>) {
        val value = repository.preValue(player) ?: newSuspendedTransaction(Dispatchers.IO) { repository.load(player) }
        if (player.isOnline) {
            repository[player.uniqueId] = value
        } else {
            repository.removePreLoad(player.uniqueId)
            repository.remove(player.uniqueId)
        }
    }

    private suspend fun <T : Any> onSave(player: Player, repository: PlayerRepository<T>) {
        val value = repository[player.uniqueId] ?: return
        newSuspendedTransaction(Dispatchers.IO) {
            repository.save(player, value)
        }
    }

    private suspend fun saveAndClear(player: Player) = coroutineScope {
        val exceptionHandler = coroutineScope.coroutineContext[CoroutineExceptionHandler]
        playerRepositoryRegistry.getAll().map { repo ->
            launch(CoroutineName("save:${repo::class.simpleName}")) {
                runCatching {
                    onSave(player, repo)
                }.onFailure { exceptionHandler?.handleException(coroutineScope.coroutineContext, it) }
                repo.removePreLoad(player.uniqueId)
                repo.remove(player.uniqueId)
            }
        }.joinAll()
    }

    // proxied server
    @Subscribe
    fun onPacketReceive(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        if (packet !is PlayerConnectionPacket) {
            return
        }

        if (packet.state == PlayerConnectionState.PRE_CONNECT) {
            loadPlayer.add(packet.player.getUniqueId())
            return
        }

        if (packet.state == PlayerConnectionState.CONNECTED) {
            if (packet.sourceChannel?.getPort() != server.port) {
                loadPlayer.remove(packet.player.getUniqueId())
            }
            return
        }

        when (packet.state) {
            PlayerConnectionState.PRE_SWITCH_CHANNEL -> {
                val nextChannel = packet.sourceChannel ?: return
                if (nextChannel.getPort() == server.port) {
                    switchGate.ensure(packet.player.getUniqueId())
                    coroutineScope.launch(Dispatchers.IO) {
                        lock(packet.player.getUniqueId())
                    }
                } else if (packet.player.getChannel()?.getPort() == server.port) {
                    val player =
                        server.getPlayer(packet.player.getUniqueId()) ?: throw NullPointerException("player not found")
                    playerScopes.scope(player.uniqueId).launch {
                        saveAndClear(player)
                        packetSender.sendPacket(nextChannel.getPort(), PlayerDataSavedPacket(packet.player))
                    }
                }
            }

            PlayerConnectionState.DISCONNECT -> {
                if (packet.player.getChannel()?.getPort() != server.port) return

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
        if (!nettyService.isEnable()) return
        switchGate.cancel(event.player.uniqueId)
        loadPlayer.remove(event.player.uniqueId)

        playerScopes.scope(event.player.uniqueId).launch(CoroutineName("save")) {
            val lock = disconnectDefermentLock.findLock(event.player.uniqueId)
            if (lock != null) {
                saveAndClear(event.player)
                disconnectDefermentLock.unlock(event.player.uniqueId)
            } else {
                var timedOut = false
                disconnectDefermentLock.tryLock(event.player, 1000L) { timedOut = true }.join()
                if (!timedOut) saveAndClear(event.player)
            }

            playerScopes.cancel(event.player.uniqueId)
        }
    }

    // non proxied server
    @Subscribe
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (nettyService.isEnable()) return

        playerScopes.scope(event.player.uniqueId).launch(CoroutineName("save")) {
            saveAndClear(event.player)

            playerScopes.cancel(event.player.uniqueId)
        }
    }

    @Subscribe
    fun onPreLoad(event: AsyncPlayerDataPreLoadEvent) = runBlocking blocking@{
        val playerId = event.playerId
        val ps = playerScopes.scope(playerId)

        withContext(ps.coroutineContext + CoroutineName("preLoad")) {
            if (nettyService.isEnable() && !loadPlayer.contains(playerId)) {
                val lock = switchDefermentLock.findLock(playerId)
                if (lock != null && !lock.isCancelled && lock.isActive) {
                    val ok = withTimeoutOrNull(1500) { switchGate.ensure(playerId).await() } != null
                    if (!ok) {
                        event.cancel("데이터 저장 시점을 받아오지 못하였습니다.")
                        switchGate.cancel(playerId)
                        return@withContext
                    }
                }
            }

            supervisorScope {
                playerRepositoryRegistry.getAll()
                    .map { repo -> async(Dispatchers.IO) { onPreLoad(playerId, repo) } }
                    .awaitAll()
            }
        }
    }

    @Subscribe
    fun onLoad(event: PlayerDataPreLoadEvent) {
        loadPlayer.add(event.player.uniqueId)
        playerScopes.scope(event.player.uniqueId).launch(CoroutineName("load")) {
            withContext(Dispatchers.BukkitMain) { event.player.isInvulnerable = true }
            val loadJobs = playerRepositoryRegistry.getAll().map { repository ->
                launch(Dispatchers.IO) {
                    onLoad(event.player, repository)
                }
            }

            loadJobs.joinAll()

            loadPlayer.remove(event.player.uniqueId)
            pluginManager.callEvent(PlayerRepositoryLoadedEvent(event.player))
            withContext(Dispatchers.BukkitMain) { event.player.isInvulnerable = false }
        }
    }

    @Subscribe
    fun unlockWhenSaved(event: AsyncNettyPacketReceivedEvent) {
        val packet = event.packet
        when (packet) {
            is PlayerDataSavedPacket -> coroutineScope.launch {
                unlock(packet.player.getUniqueId())
                switchGate.signal(packet.player.getUniqueId())
            }
        }
    }

    private suspend fun lock(uniqueId: UUID) {
        val lock = switchDefermentLock.findLock(uniqueId)
        if (lock != null) {
            throw IllegalStateException("Player ($uniqueId) is already locked.")
        } else {
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
            loadPlayer.add(uniqueId)
        } else {
            throw IllegalStateException("Player ($uniqueId) not locked.")
        }
    }
}