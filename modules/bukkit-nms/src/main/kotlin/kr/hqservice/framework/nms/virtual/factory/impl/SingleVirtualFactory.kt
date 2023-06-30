package kr.hqservice.framework.nms.virtual.factory.impl

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.entity.inner.VirtualCamera
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.factory.VirtualFactory
import kr.hqservice.framework.nms.virtual.factory.VirtualContainerFactory
import kr.hqservice.framework.nms.virtual.item.VirtualItem
import kr.hqservice.framework.nms.wrapper.item.NmsItemStackWrapper
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class SingleVirtualFactory(
    private val receiver: Player,
    private val reflectionWrapper: NmsReflectionWrapper,
    private val itemStackService: NmsService<ItemStack, NmsItemStackWrapper>,
) : VirtualFactory {
    @Deprecated("use inventory()")
    override suspend fun setItem(
        slot: Int,
        itemStack: ItemStack,
        itemEditBlock: ItemMeta.() -> Unit,
    ) {
        reflectionWrapper.sendPacket(receiver, VirtualItem(receiver, slot, itemStack, itemEditBlock))
    }

    override fun getViewers(): List<Player> {
        return listOf(receiver)
    }

    override suspend fun setCamera(virtualEntity: AbstractVirtualEntity?) {
        val virtualCamera = VirtualCamera(receiver, virtualEntity)
        reflectionWrapper.sendPacket(receiver, virtualCamera)
    }

    override suspend fun inventory(containerFactoryScope: VirtualContainerFactory.() -> Unit) {
        val containerFactory = VirtualContainerFactory(receiver)
        containerFactory.containerFactoryScope()
        reflectionWrapper.sendPacket(receiver, *containerFactory.getMessages())
    }

    override suspend fun updateEntity(virtualEntity: AbstractVirtualEntity) {
        reflectionWrapper.sendPacket(receiver, virtualEntity)
    }
}