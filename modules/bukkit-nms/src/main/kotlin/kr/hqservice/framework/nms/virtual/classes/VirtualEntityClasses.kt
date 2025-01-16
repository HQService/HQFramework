package kr.hqservice.framework.nms.virtual.classes

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot

@Component
@Singleton(binds = [VirtualEntityClasses::class])
interface VirtualEntityClasses : HQSimpleComponent {
    val entitySpawnPacket : VirtualMessageConstructor
    val entityDestroyPacket : VirtualMessageConstructor
    val entityTeleportPacket : VirtualMessageConstructor
    val entityEquipmentPacket : VirtualMessageConstructor

    fun createMetaDataPacket(entity: Any): Any
    fun getId(entity: Any): Int
    fun setCustomName(name: String, entity: Any)
    fun setCustomName(name: BaseComponent, entity: Any)
    fun setCustomNameVisible(visible: Boolean, entity: Any)
    fun setLocation(location: Location, entity: Any)
    fun setInvisible(invisible: Boolean, entity: Any)
    fun setGlowing(glowing: Boolean, entity: Any)
    fun getEnumItemSlot(enumItemSlot: String): Any
    fun getEnumItemSlot(enumItemSlot: EquipmentSlot): Any
    fun createBukkitPair(first: Any, second: Any): Any
}