package kr.hqservice.framework.nms.virtual.factory.impl

import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualCamera
import kr.hqservice.framework.nms.virtual.factory.VirtualContainerFactory
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import kr.hqservice.framework.nms.virtual.world.VirtualWorldBorder
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class GlobalVirtualFactory(
    private val receivers: List<Player>,
    private val reflectionWrapper: NmsReflectionWrapper,
) : VirtualFactory {
    @Deprecated("use inventory()")
    override suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit
    ) {
        receivers.forEach {
            reflectionWrapper.sendPacket(it, VirtualItem(it, slot, itemStack, itemEditBlock))
        }
    }

    override fun getViewers(): List<Player> {
        return receivers
    }

    override suspend fun setCamera(virtualEntity: AbstractVirtualEntity?) {
        receivers.forEach {
            val virtualCamera = VirtualCamera(it, virtualEntity, reflectionWrapper)
            reflectionWrapper.sendPacket(it, virtualCamera)
        }
    }

    override suspend fun inventory(containerFactoryScope: VirtualContainerFactory.() -> Unit) {
        receivers.forEach {
            val containerFactory = VirtualContainerFactory(it)
            containerFactory.containerFactoryScope()
            reflectionWrapper.sendPacket(it, *containerFactory.getMessages())
        }
    }

    override suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(receivers, virtualEntity)
    }

    override suspend fun setWorldBorder(virtualWorldBorder: VirtualWorldBorder) {
        reflectionWrapper.sendPacket(receivers, virtualWorldBorder)
    }
}