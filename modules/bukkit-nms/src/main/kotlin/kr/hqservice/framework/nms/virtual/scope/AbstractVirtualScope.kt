package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualCamera
import kr.hqservice.framework.nms.virtual.handler.impl.VirtualAnvilHandler
import kr.hqservice.framework.nms.virtual.registry.VirtualHandlerRegistry
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
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
        title: String = "",
        anvilFactoryScope: VirtualAnvilContainerScope.() -> Unit
    ) {
        viewers.forEach {
            val containerFactory = VirtualAnvilContainerScope(it, title)
            containerFactory.anvilFactoryScope()
            reflectionWrapper.sendPacket(it, *containerFactory.getMessages())
            val dummyListener = object : Listener {}

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
                lazyPlugin.server.scheduler.runTask(lazyPlugin, Runnable { it.closeInventory() })
                InventoryClickEvent.getHandlerList().unregister(dummyListener)
            } }, {
                reflectionWrapper.sendPacket(it, *containerFactory.getBaseItem().toTypedArray())
            }))
        }
    }

    open suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(viewers, virtualEntity)
    }

    suspend fun updateWorldBorder(virtualWorldBorder: VirtualWorldBorder) {
        reflectionWrapper.sendPacket(viewers, virtualWorldBorder)
    }
}