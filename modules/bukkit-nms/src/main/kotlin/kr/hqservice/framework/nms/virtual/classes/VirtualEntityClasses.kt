package kr.hqservice.framework.nms.virtual.classes

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kr.hqservice.framework.nms.wrapper.getStaticFunction
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot
import org.koin.core.annotation.Named

@Component
@HQSingleton(binds = [VirtualEntityClasses::class])
class VirtualEntityClasses(
    reflectionWrapper: NmsReflectionWrapper,
    @Named("base-component") private val componentWrapper: NmsService<String, BaseComponentWrapper>,
) : HQSimpleComponent {
    private val entityClass = reflectionWrapper.getNmsClass("EntityLiving",
        Version.V_15.handle("world.entity.Entity", true))
    private val dataWatcherClass = reflectionWrapper.getNmsClass("DataWatcher",
        Version.V_15.handle("network.syncher"))
    internal val entitySpawnPacket = reflectionWrapper.getNmsClass("PacketPlayOutSpawnEntityLiving",
        Version.V_15.handle("network.protocol.game.PacketPlayOutSpawnEntity", true)).java.getConstructor(entityClass.java)
    internal val entityDestroyPacket = reflectionWrapper.getNmsClass("PacketPlayOutEntityDestroy",
        Version.V_15.handle("network.protocol.game")).java.getConstructor(IntArray::class.java)
    internal val entityTeleportPacket = reflectionWrapper.getNmsClass("PacketPlayOutEntityTeleport",
        Version.V_15.handle("network.protocol.game")).java.getConstructor(entityClass.java)
    internal val entityEquipmentPacket =
        reflectionWrapper.getNmsClass("PacketPlayOutEntityEquipment", Version.V_15.handle("network.protocol.game")).java.getConstructor(Int::class.java, List::class.java)
    private val listMetadata = Version.V_19.support(reflectionWrapper.getVersion())
    private val entityMetadataPacket = if(!listMetadata) {
        reflectionWrapper.getNmsClass("PacketPlayOutEntityMetadata",
            Version.V_15.handle("network.protocol.game")).java.getConstructor(Int::class.java, dataWatcherClass.java, Boolean::class.java)
    } else {
        reflectionWrapper.getNmsClass("PacketPlayOutEntityMetadata",
            Version.V_15.handle("network.protocol.game")).java.getConstructor(Int::class.java, List::class.java)
    }
    private val getDataWatcherFunction = reflectionWrapper.getFunction(entityClass, "getDataWatcher",
        Version.V_15.handleFunction("V"),
        Version.V_17.handleFunction("ad"),
        Version.V_19.handleFunction("ai"),
        Version.V_19_3.handleFunction("al"),
        Version.V_19_4.handleFunction("aj"))
    private val nonDefaultValueFunction = if(listMetadata) {
        dataWatcherClass.java.getMethod("c")
    } else null
    private val getIdFunction = reflectionWrapper.getFunction(entityClass, "getId",
        Version.V_15.handleFunction("S"),
        Version.V_17.handleFunction("Z"),
        Version.V_19.handleFunction("ae"),
        Version.V_19_3.handleFunction("ah"),
        Version.V_19_4.handleFunction("af")
    )
    private val setCustomNameFunction = reflectionWrapper.getFunction(entityClass, "setCustomName", listOf(componentWrapper.getTargetClass()),
        Version.V_19.handleFunction("b") { setParameterClasses(componentWrapper.getTargetClass())})

    private val setLocationFunction = reflectionWrapper.getFunction(entityClass, "setLocation", listOf(Double::class, Double::class, Double::class, Float::class, Float::class),
        Version.V_15.handleFunction("a") {setParameterClasses(Double::class, Double::class, Double::class, Float::class, Float::class) })

    private val setCustomNameVisibleFunction = reflectionWrapper.getFunction(entityClass, "setCustomNameVisible", listOf(Boolean::class),
        Version.V_15.handleFunction("n") { setParameterClasses(Boolean::class) })

    private val setInvisibleFunction = reflectionWrapper.getFunction(entityClass, "setInvisible", listOf(Boolean::class),
        Version.V_15.handleFunction("j") { setParameterClasses(Boolean::class) })

    private val enumItemSlotClass = reflectionWrapper.getNmsClass("EnumItemSlot", Version.V_15.handle("world.entity"))
    private val enumItemSlotValueOfFunction = reflectionWrapper.getStaticFunction(enumItemSlotClass, "valueOf", listOf(String::class),
        Version.V_15.handleFunction("a") {
            setParameterClasses(String::class)
            static()
        })
    private val pairConstructor = Class.forName("com.mojang.datafixers.util.Pair").getConstructor(Any::class.java, Any::class.java)

    private fun getDataWatcherSingle(entity: Any): Any {
        return getDataWatcherFunction.call(entity)?: throw NoSuchMethodException("Entity#getDataWatcher() 메소드를 찾을 수 없습니다.")
    }

    private fun toNonDefaultValue(dataWatcher: Any): Any {
        return nonDefaultValueFunction?.invoke(dataWatcher)?: throw NoSuchMethodException("DataWatcher#nonDefaultValue() 메소드를 찾을 수 없습니다.")
    }

    private fun getDataWatcher(entity: Any): Any {
        val watcher = getDataWatcherSingle(entity)
        return if(listMetadata) toNonDefaultValue(watcher) else watcher
    }

    fun createMetaDataPacket(entity: Any): Any {
        val entityId = getId(entity)
        return if(listMetadata) entityMetadataPacket.newInstance(entityId, getDataWatcher(entity))
        else entityMetadataPacket.newInstance(entityId, getDataWatcher(entity), true)
    }

    fun getId(entity: Any): Int {
        return getIdFunction.call(entity) as? Int?: throw NoSuchMethodException("Entity#getId() 메소드를 찾을 수 없습니다.")
    }

    fun setCustomName(name: String, entity: Any) {
        setCustomNameFunction.call(entity, componentWrapper.wrap(name).getUnwrappedInstance())
    }

    fun setCustomNameVisible(visible: Boolean, entity: Any) {
        setCustomNameVisibleFunction.call(entity, visible)
    }

    fun setLocation(location: Location, entity: Any) {
        setLocationFunction.call(entity, location.x, location.y, location.z, location.yaw, location.pitch)
    }

    fun setInvisible(invisible: Boolean, entity: Any) {
        setInvisibleFunction.call(entity, invisible)
    }

    fun getEnumItemSlot(enumItemSlot: String): Any {
        return enumItemSlotValueOfFunction.call(enumItemSlot.lowercase())?: throw NoSuchElementException()
    }

    fun getEnumItemSlot(enumItemSlot: EquipmentSlot): Any {
        return enumItemSlotValueOfFunction.call(enumItemSlot.name.lowercase())?: throw NoSuchElementException()
    }

    fun createBukkitPair(first: Any, second: Any): Any {
        return pairConstructor.newInstance(first, second)
    }
}