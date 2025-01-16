package kr.hqservice.framework.nms.virtual.classes

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.getStaticFunction
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot

@Component
@Singleton(binds = [VirtualEntityClasses::class])
interface VirtualEntityClasses<T : Any> : HQSimpleComponent {
    val entitySpawnPacket : VirtualMessageConstructor<T>
    val entityDestroyPacket : VirtualMessageConstructor<T>
    val entityTeleportPacket : VirtualMessageConstructor<T>
    val entityEquipmentPacket : VirtualMessageConstructor<T>

    fun createMetaDataPacket(entity: Any): T
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