package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.VirtualCamera
import kr.hqservice.framework.nms.virtual.entity.VirtualEntityFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualAnvilHandlerFactory
import kr.hqservice.framework.nms.virtual.handler.VirtualSignHandlerFactory
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.getKoin

private val handlerRegistry: VirtualHandlerRegistry by getKoin().inject()
private val anvilHandlerFactory: VirtualAnvilHandlerFactory by getKoin().inject()
private val signHandlerFactory: VirtualSignHandlerFactory by getKoin().inject()
private val cameraFactory: VirtualEntityFactory by getKoin().inject(named("virtual.entity.camera"))
private val signFactory: VirtualEntityFactory by getKoin().inject(named("virtual.entity.sign"))

private val lazyPlugin by lazy { Bukkit.getPluginManager().getPlugin("HQFramework")!! }

abstract class AbstractVirtualScope(
    val viewers: List<Player>,
    protected val reflectionWrapper: NmsReflectionWrapper
) {
    suspend fun setCamera(virtualEntity: AbstractVirtualEntity?) {
        viewers.forEach {
            val virtualCamera = VirtualCamera(it, virtualEntity, cameraFactory)
            reflectionWrapper.sendPacket(it, virtualCamera)
        }
    }

    suspend fun inventory(containerFactoryScope: VirtualContainerScope.() -> Unit) {
        viewers.forEach {
            val containerFactory = VirtualContainerScope(it)
            containerFactory.containerFactoryScope()
            reflectionWrapper.sendPacket(it, *containerFactory.getMessages())
        }
    }

    private suspend fun anvilLogic(player: Player, containerFactory: VirtualAnvilContainerScope, anvilFactoryScope: VirtualAnvilContainerScope.() -> Unit) {
        containerFactory.anvilFactoryScope()
        reflectionWrapper.sendPacket(player, *containerFactory.getMessages())
        val dummyListener = anvilHandlerFactory.createListener(player, lazyPlugin)

        lazyPlugin.server.pluginManager.registerEvent(InventoryClickEvent::class.java, dummyListener, EventPriority.LOWEST, { _, event ->
            event as InventoryClickEvent
            if (event.whoClicked.uniqueId == player.uniqueId) {
                event.isCancelled = true
                event.setCursor(null)
            }
        }, lazyPlugin)

        handlerRegistry.register(player.uniqueId, anvilHandlerFactory.createHandler(reflectionWrapper, { text ->
            val resultItem = containerFactory.getResultItem(text)
            reflectionWrapper.sendPacket(player, resultItem)
        }, { text -> if (containerFactory.confirm(text)) {
            lazyPlugin.getScheduler().runTask {
                player.closeInventory()
                player.updateInventory()
            }
            true
        } else false }, { slot, text -> if (containerFactory.button(slot, text)) {
            lazyPlugin.getScheduler().runTask {
                player.closeInventory()
                player.updateInventory()
            }
            true
        } else false }, {
            reflectionWrapper.sendPacket(player, *containerFactory.getBaseItem().toTypedArray())
        }, dummyListener, { text ->
            containerFactory.close(text)
        }))
    }

    suspend fun anvil(
        title: Component,
        anvilFactoryScope: VirtualAnvilContainerScope.() -> Unit
    ) {
        viewers.forEach {
            val containerFactory = VirtualAnvilContainerScope(it, title)
            anvilLogic(it, containerFactory, anvilFactoryScope)
        }
    }

    suspend fun anvil(
        title: BaseComponent = TextComponent(""),
        anvilFactoryScope: VirtualAnvilContainerScope.() -> Unit
    ) {
        viewers.forEach {
            val containerFactory = VirtualAnvilContainerScope(it, title)
            anvilLogic(it, containerFactory, anvilFactoryScope)
        }
    }

    suspend fun sign(
        signFactoryScope: VirtualSignScope.() -> Unit
    ) {
        viewers.forEach { player ->
            val containerFactory = VirtualSignScope(player, signFactory)
            containerFactory.signFactoryScope()

            val server = player.server
            val location = player.location
            val blockData = server.createBlockData(Material.OAK_SIGN)
            player.sendBlockChange(location, blockData)
            reflectionWrapper.sendPacket(player, *containerFactory.getMessages())

            val virtualSignHandler = signHandlerFactory.createHandler(reflectionWrapper) { texts ->
                if (containerFactory.confirm(texts)) {
                    val airBlockData = server.createBlockData(Material.AIR)
                    player.sendBlockChange(location, airBlockData)
                    true
                } else {
                    sign(signFactoryScope)
                    false
                }
            }
            handlerRegistry.register(player.uniqueId, virtualSignHandler)
        }
    }

    open suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(viewers, virtualEntity)
    }

    suspend fun updateWorldBorder(virtualWorldBorder: VirtualWorldBorder) {
        reflectionWrapper.sendPacket(viewers, virtualWorldBorder)
    }
}