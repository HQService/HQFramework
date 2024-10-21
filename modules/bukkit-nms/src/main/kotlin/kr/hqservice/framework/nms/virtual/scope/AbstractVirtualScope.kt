package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.bukkit.core.scheduler.getScheduler
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualCamera
import kr.hqservice.framework.nms.virtual.handler.impl.VirtualAnvilHandler
import kr.hqservice.framework.nms.virtual.handler.impl.VirtualSignHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.koin.java.KoinJavaComponent.getKoin

private val handlerRegistry: VirtualHandlerRegistry by getKoin().inject()
private val lazyPlugin by lazy { Bukkit.getPluginManager().getPlugin("HQFramework")!! }

abstract class AbstractVirtualScope(
    val viewers: List<Player>,
    protected val reflectionWrapper: NmsReflectionWrapper
) {
    suspend fun setCamera(virtualEntity: AbstractVirtualEntity?) {
        viewers.forEach {
            val virtualCamera = VirtualCamera(it, virtualEntity, reflectionWrapper)
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

    suspend fun anvil(
        title: BaseComponent = TextComponent(""),
        anvilFactoryScope: VirtualAnvilContainerScope.() -> Unit
    ) {
        viewers.forEach {
            val containerFactory = VirtualAnvilContainerScope(it, title)
            containerFactory.anvilFactoryScope()
            reflectionWrapper.sendPacket(it, *containerFactory.getMessages())
            val dummyListener = VirtualAnvilHandler.VirtualAnvilListener(it, lazyPlugin)

            lazyPlugin.server.pluginManager.registerEvent(InventoryClickEvent::class.java, dummyListener, EventPriority.LOWEST, { _, event ->
                event as InventoryClickEvent
                if (event.whoClicked.uniqueId == it.uniqueId) {
                    event.isCancelled = true
                    event.cursor = null
                }
            }, lazyPlugin)

            handlerRegistry.register(it.uniqueId, VirtualAnvilHandler(reflectionWrapper, { text ->
                val resultItem = containerFactory.getResultItem(text)
                reflectionWrapper.sendPacket(it, resultItem)
            }, { text -> if (containerFactory.confirm(text)) {
                lazyPlugin.getScheduler().runTask {
                    it.closeInventory()
                    it.updateInventory()
                }
                true
            } else false }, { slot, text -> if (containerFactory.button(slot, text)) {
                lazyPlugin.getScheduler().runTask {
                    it.closeInventory()
                    it.updateInventory()
                }
                true
            } else false }, {
                reflectionWrapper.sendPacket(it, *containerFactory.getBaseItem().toTypedArray())
            }, dummyListener, { text ->
                containerFactory.close(text)
            }))
        }
    }

    suspend fun sign(
        signFactoryScope: VirtualSignScope.() -> Unit
    ) {
        viewers.forEach { player ->
            val containerFactory = VirtualSignScope(player, reflectionWrapper)
            containerFactory.signFactoryScope()

            val server = player.server
            val location = player.location
            val blockData = server.createBlockData(Material.OAK_SIGN)
            player.sendBlockChange(location, blockData)
            reflectionWrapper.sendPacket(player, *containerFactory.getMessages())

            val virtualSignHandler = VirtualSignHandler(reflectionWrapper) { texts ->
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