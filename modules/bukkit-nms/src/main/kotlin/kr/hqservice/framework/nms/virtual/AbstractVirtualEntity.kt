package kr.hqservice.framework.nms.virtual

import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.nms.extension.getNmsItemStack
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.message.VirtualListMessage
import kr.hqservice.framework.nms.virtual.message.VirtualMessageImpl
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class AbstractVirtualEntity(
    private var location: Location,
    private var name: String,
) : Virtual, KoinComponent {
    protected val reflectionWrapper: NmsReflectionWrapper by inject()
    private val virtualEntityClasses: VirtualEntityClasses by inject()

    private var state: Byte = 0x7
    private var itemContainer: List<Any>? = null
    private var vaild = false
    abstract fun getEntity(): Any
    private fun entityInitialize() {
        if (name.isNotEmpty()) {
            setName(name.colorize())
            setNameVisible(true)
        } else setNameVisible(false)
        initialize()
        vaild = true
    }

    protected abstract fun initialize()

    private fun getEntityId(): Int {
        return virtualEntityClasses.getId(getEntity())
    }

    fun getLocation(): Location {
        return location.clone()
    }

    fun setLocation(location: Location) {
        if (location == this.location) return
        try {
            virtualEntityClasses.setLocation(location, getEntity())
            this.location = location
            switchLocationMask()
            switchMetaMask()
        } catch (e: Exception) {
            throw UnsupportedOperationException("VirtualEntity#setLocation() 메소드를 실행할 수 없습니다.", e)
        }
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        virtualEntityClasses.setCustomName(name, getEntity())
        this.name = name
        switchMetaMask()
    }

    fun setNameVisible(visible: Boolean) {
        try {
            virtualEntityClasses.setCustomNameVisible(visible, getEntity())
            switchMetaMask()
        } catch (e: Exception) {
            throw UnsupportedOperationException("VirtualEntity#setNameVisible(Boolean) 메소드를 실행할 수 없습니다.", e)
        }
    }

    fun setInvisible(invisible: Boolean) {
        virtualEntityClasses.setInvisible(invisible, getEntity())
        switchMetaMask()
    }

    fun setEquipmentItems(items: List<Pair<EquipmentSlot, ItemStack>>) {
        itemContainer = items.map {
            val slot =
                virtualEntityClasses.getEnumItemSlot(if (it.first == EquipmentSlot.HAND) "mainhand" else it.first.name)
            virtualEntityClasses.createBukkitPair(
                slot,
                it.second.getNmsItemStack().getUnwrappedInstance()
            )
        }
        if (!(state mask VirtualEntityState.UPDATE_ITEM))
            state = state switch VirtualEntityState.UPDATE_ITEM
        switchMetaMask()
    }

    fun destroy() {
        if (!(state mask VirtualEntityState.DESTROY))
            state = state switch VirtualEntityState.DESTROY
    }

    private fun switchLocationMask() {
        if (!(state mask VirtualEntityState.RELOCATE))
            state = state switch VirtualEntityState.RELOCATE
    }

    protected fun switchMetaMask() {
        if (!(state mask VirtualEntityState.UPDATE_META_DATA))
            state = state switch VirtualEntityState.UPDATE_META_DATA
    }

    internal fun createVirtualMessage(switchState: Boolean): VirtualMessage? {
        if (state mask VirtualEntityState.UNHANDLED) return null

        val packets = mutableListOf<Any>()
        if (state mask VirtualEntityState.DESTROY) {
            if (switchState) state = state switch VirtualEntityState.UNHANDLED
            return VirtualMessageImpl(virtualEntityClasses.entityDestroyPacket.newInstance(arrayOf(getEntityId()).toIntArray()))
        }

        if (state mask VirtualEntityState.CREAT) {
            if (!vaild) entityInitialize()
            if (switchState) state = state switch VirtualEntityState.CREAT
            packets.add(virtualEntityClasses.entitySpawnPacket.newInstance(getEntity()))
        }

        if (state mask VirtualEntityState.UPDATE_ITEM) {
            if (switchState) state = state switch VirtualEntityState.UPDATE_ITEM
            packets.add(
                virtualEntityClasses.entityEquipmentPacket.newInstance(
                    getEntityId(),
                    itemContainer ?: emptyList<Any>()
                )
            )
        }

        if (state mask VirtualEntityState.RELOCATE) {
            if (switchState) state = state switch VirtualEntityState.RELOCATE
            packets.add(virtualEntityClasses.entityTeleportPacket.newInstance(getEntity()))
        }

        if (state mask VirtualEntityState.UPDATE_META_DATA) {
            if (switchState) state = state switch VirtualEntityState.UPDATE_META_DATA
            packets.add(virtualEntityClasses.createMetaDataPacket(getEntity()))
        }
        return if (packets.isEmpty()) null
        else if (packets.size == 1) VirtualMessageImpl(packets.first())
        else VirtualListMessage(packets)
    }

    final override fun createVirtualMessage(): VirtualMessage? {
        return createVirtualMessage(true)
    }
}