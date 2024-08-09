package kr.hqservice.framework.nms.virtual.classes

import com.mojang.datafixers.util.Pair
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot

@Component
@Singleton(binds = [VirtualEntityClasses::class])
class VirtualEntityClasses(
    @Qualifier("base-component") private val componentWrapper: NmsService<String, BaseComponentWrapper>,
) : HQSimpleComponent {
    private fun getDataWatcherSingle(entity: Entity): SynchedEntityData {
        return entity.entityData
    }

    private fun toNonDefaultValue(dataWatcher: SynchedEntityData): List<SynchedEntityData.DataValue<*>> {
        return dataWatcher.nonDefaultValues ?: throw NullPointerException()
    }

    private fun getDataWatcher(entity: Entity): List<SynchedEntityData.DataValue<*>> {
        val watcher = getDataWatcherSingle(entity)
        return toNonDefaultValue(watcher)
    }

    fun createMetaDataPacket(entity: Entity): Packet<*> {
        val entityId = getId(entity)
        return ClientboundSetEntityDataPacket(entityId, getDataWatcher(entity))
    }

    fun getId(entity: Entity): Int {
        return entity.id
    }

    fun setCustomName(name: String, entity: Entity) {
        entity.customName = componentWrapper.wrap("{\"text\":\"$name\"}").getUnwrappedInstance() as net.minecraft.network.chat.Component
    }

    fun setCustomName(name: BaseComponent, entity: Entity) {
        entity.customName = componentWrapper.wrap(ComponentSerializer.toString(name)).getUnwrappedInstance() as net.minecraft.network.chat.Component
    }

    fun setCustomNameVisible(visible: Boolean, entity: Entity) {
        entity.isCustomNameVisible = visible
    }

    fun setLocation(location: Location, entity: Entity) {
        entity.absMoveTo(location.x, location.y, location.z, location.yaw, location.pitch)
    }

    fun setInvisible(invisible: Boolean, entity: Entity) {
        entity.isInvisible = invisible
    }

    fun setGlowing(glowing: Boolean, entity: Entity) {
        entity.setGlowingTag(glowing)
    }

    fun getEnumItemSlot(enumItemSlot: String): net.minecraft.world.entity.EquipmentSlot {
        return net.minecraft.world.entity.EquipmentSlot.byName(enumItemSlot.lowercase())
    }

    fun getEnumItemSlot(enumItemSlot: EquipmentSlot): net.minecraft.world.entity.EquipmentSlot {
        return net.minecraft.world.entity.EquipmentSlot.byName(enumItemSlot.name)
    }

    fun createBukkitPair(first: net.minecraft.world.entity.EquipmentSlot, second: ItemStack): Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack> {
        return Pair.of(first, second)
    }
}