package kr.hqservice.framework.nms.virtual.scope

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualCamera
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

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

    open suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(viewers, virtualEntity)
    }

    suspend fun updateWorldBorder(virtualWorldBorder: VirtualWorldBorder) {
        reflectionWrapper.sendPacket(viewers, virtualWorldBorder)
    }
}