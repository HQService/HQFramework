package kr.hqservice.framework.nms.v21_11.virtual.classes

import com.mojang.datafixers.util.Pair
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.virtual.classes.VirtualMessageConstructor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.network.ServerPlayerConnection
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.item.ItemStack
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot

class VirtualEntityClassesImpl(
    private val componentWrapper: NmsBaseComponentService,
) : VirtualEntityClasses {
    override val entitySpawnPacket: VirtualMessageConstructor = VirtualMessageConstructor {
        val npcServerEntity = ServerEntity(
            (it.getEntity() as Entity).level() as ServerLevel,
            (it.getEntity() as Entity),
            0, false,
            (it.getEntity() as Entity).`moonrise$getTrackedEntity`(),
            emptySet(),
        )

        ClientboundAddEntityPacket(it.getEntity() as Entity, npcServerEntity)
    }

    override val entityDestroyPacket: VirtualMessageConstructor = VirtualMessageConstructor {
        ClientboundRemoveEntitiesPacket(it.getEntityId())
    }
    override val entityTeleportPacket: VirtualMessageConstructor = VirtualMessageConstructor {
        ClientboundTeleportEntityPacket(
            (it.getEntity() as Entity).id,
            (it.getEntity() as Entity).let { PositionMoveRotation.of(it) },
            emptySet(), (it.getEntity() as Entity).onGround
        )
    }

    @Suppress("unchecked_cast")
    override val entityEquipmentPacket: VirtualMessageConstructor = VirtualMessageConstructor {
        ClientboundSetEquipmentPacket(it.getEntityId(), it.itemContainer as? List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> ?: emptyList<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>>())
    }

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

    override fun createMetaDataPacket(entity: Any): Any {
        val entityId = getId(entity)
        return ClientboundSetEntityDataPacket(entityId, getDataWatcher(entity as Entity))
    }

    override fun getId(entity: Any): Int {
        return (entity as Entity).id
    }

    override fun setCustomName(name: String, entity: Any) {
        entity as Entity
        entity.customName = componentWrapper.wrap("{\"text\":\"$name\"}").getUnwrappedInstance() as net.minecraft.network.chat.Component
    }

    override fun setCustomName(name: BaseComponent, entity: Any) {
        entity as Entity
        entity.customName = componentWrapper.wrap(ComponentSerializer.toString(name)).getUnwrappedInstance() as net.minecraft.network.chat.Component
    }

    override fun setCustomNameVisible(visible: Boolean, entity: Any) {
        entity as Entity
        entity.isCustomNameVisible = visible
    }

    override fun setLocation(location: Location, entity: Any) {
        entity as Entity
        entity.absSnapTo(location.x, location.y, location.z, location.yaw, location.pitch)
    }

    override fun setInvisible(invisible: Boolean, entity: Any) {
        entity as Entity
        entity.isInvisible = invisible
    }

    override fun setGlowing(glowing: Boolean, entity: Any) {
        entity as Entity
        entity.setGlowingTag(glowing)
    }

    override fun getEnumItemSlot(enumItemSlot: String): net.minecraft.world.entity.EquipmentSlot {
        return net.minecraft.world.entity.EquipmentSlot.byName(enumItemSlot.lowercase())
    }

    override fun getEnumItemSlot(enumItemSlot: EquipmentSlot): net.minecraft.world.entity.EquipmentSlot {
        return net.minecraft.world.entity.EquipmentSlot.byName(enumItemSlot.name)
    }

    override fun createBukkitPair(first: Any, second: Any): Any {
        return Pair.of(first, second)
    }
}