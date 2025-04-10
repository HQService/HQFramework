package kr.hqservice.framework.nms.legacy.virtual.classes

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.virtual.classes.VirtualEntityClasses
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.legacy.wrapper.getStaticFunction
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.virtual.classes.VirtualMessageConstructor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Location
import org.bukkit.inventory.EquipmentSlot

class LegacyVirtualEntityClasses(
    reflectionWrapper: LegacyNmsReflectionWrapper,
    private val componentWrapper: NmsBaseComponentService,
) : VirtualEntityClasses {
    private val entityClass = reflectionWrapper.getNmsClass(
        "EntityLiving",
        Version.V_17.handle("world.entity.Entity", true)
    )

    private val dataWatcherClass = reflectionWrapper.getNmsClass(
        "DataWatcher",
        Version.V_17.handle("network.syncher")
    )

    private val entitySpawnPacketConstructor = reflectionWrapper.getNmsClass("PacketPlayOutSpawnEntityLiving",
        Version.V_17.handle("network.protocol.game.PacketPlayOutSpawnEntity", true)
    ).java.getConstructor(entityClass.java)

    private val entityDestroyPacketConstructor = reflectionWrapper.getNmsClass("PacketPlayOutEntityDestroy",
        Version.V_17.handle("network.protocol.game")
    ).java.getConstructor(IntArray::class.java)

    private val entityTeleportPacketConstructor = reflectionWrapper.getNmsClass("PacketPlayOutEntityTeleport",
        Version.V_17.handle("network.protocol.game")
    ).java.getConstructor(entityClass.java)

    private val entityEquipmentPacketConstructor = reflectionWrapper.getNmsClass("PacketPlayOutEntityEquipment",
        Version.V_17.handle("network.protocol.game")
    ).java.getConstructor(Int::class.java, List::class.java)

    override val entitySpawnPacket = VirtualMessageConstructor {
        entitySpawnPacketConstructor.newInstance(it.getEntity())
    }

    override val entityDestroyPacket = VirtualMessageConstructor {
        entityDestroyPacketConstructor.newInstance(intArrayOf(it.getEntityId()))
    }

    override val entityTeleportPacket = VirtualMessageConstructor {
        entityTeleportPacketConstructor.newInstance(it.getEntity())
    }

    override val entityEquipmentPacket = VirtualMessageConstructor {
        entityEquipmentPacketConstructor.newInstance(it.getEntityId(), it.itemContainer ?: emptyList<Any>())
    }

    private val listMetadata = Version.V_19.support(reflectionWrapper.getVersion())
    private val entityMetadataPacket = if (!listMetadata) {
        reflectionWrapper.getNmsClass("PacketPlayOutEntityMetadata",
            Version.V_17.handle("network.protocol.game")
        ).java.getConstructor(Int::class.java, dataWatcherClass.java, Boolean::class.java)
    } else {
        reflectionWrapper.getNmsClass("PacketPlayOutEntityMetadata",
            Version.V_17.handle("network.protocol.game")
        ).java.getConstructor(Int::class.java, List::class.java)
    }

    private val getDataWatcherFunction = reflectionWrapper.getFunction(entityClass, "getDataWatcher",
        Version.V_18.handleFunction("ai"),
        Version.V_19_3.handleFunction("al"),
        Version.V_19_4.handleFunction("aj"),
        Version.V_20_2.handleFunction("al"),
        Version.V_20_4.handleFunction("an"),
        Version.V_17_FORGE.handleFunction("m_20088_")
    )

    private val nonDefaultValueFunction = if (listMetadata) {
        dataWatcherClass.java.getMethod("c")
    } else {
        null
    }

    private val getIdFunction = reflectionWrapper.getFunction(entityClass, "getId",
        Version.V_18.handleFunction("ae"),
        Version.V_19_3.handleFunction("ah"),
        Version.V_19_4.handleFunction("af"),
        Version.V_20_2.handleFunction("ah"),
        Version.V_20_4.handleFunction("aj"),
        Version.V_17_FORGE.handleFunction("m_142049_"),
        Version.V_19_FORGE.handleFunction("m_19879_")
    )

    private val setCustomNameFunction = reflectionWrapper.getFunction(entityClass, "setCustomName", listOf(componentWrapper.getTargetClass()),
        Version.V_18.handleFunction("a") { setParameterClasses(componentWrapper.getTargetClass()) },
        Version.V_19.handleFunction("b") { setParameterClasses(componentWrapper.getTargetClass()) },
        Version.V_17_FORGE.handleFunction("m_6593_") { setParameterClasses(componentWrapper.getTargetClass()) }
    )

    private val setCustomNameVisibleFunction = reflectionWrapper.getFunction(entityClass, "setCustomNameVisible", listOf(Boolean::class),
        Version.V_18.handleFunction("n") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_20340_") { setParameterClasses(Boolean::class) }
    )

    private val setLocationFunction = reflectionWrapper.getFunction(entityClass, "setLocation",
        listOf(Double::class, Double::class, Double::class, Float::class, Float::class),
        Version.V_18.handleFunction("a") {
            setParameterClasses(Double::class, Double::class, Double::class, Float::class, Float::class)
        },
        Version.V_17_FORGE.handleFunction("m_19890_") {
            setParameterClasses(Double::class, Double::class, Double::class, Float::class, Float::class)
        }
    )

    private val setInvisibleFunction = reflectionWrapper.getFunction(entityClass, "setInvisible", listOf(Boolean::class),
        Version.V_18.handleFunction("j") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_6842_") { setParameterClasses(Boolean::class) }
    )

    private val setGlowingTagFunction = reflectionWrapper.getFunction(entityClass, "setGlowingTag", listOf(Boolean::class),
        Version.V_18.handleFunction("i") { setParameterClasses(Boolean::class) },
        Version.V_17_FORGE.handleFunction("m_146915_") { setParameterClasses(Boolean::class) }
    )

    private val enumItemSlotClass = reflectionWrapper.getNmsClass("EnumItemSlot",
        Version.V_17.handle("world.entity")
    )
    private val enumItemSlotValueOfFunction = reflectionWrapper.getStaticFunction(enumItemSlotClass, "fromName", listOf(String::class),
        Version.V_18.handleFunction("a") {
            setParameterClasses(String::class)
            static()
        },
        Version.V_17_FORGE.handleFunction("m_20747_") {
            setParameterClasses(String::class)
            static()
        }
    )

    private val pairConstructor =
        Class.forName("com.mojang.datafixers.util.Pair").getConstructor(Any::class.java, Any::class.java)

    private fun getDataWatcherSingle(entity: Any): Any {
        return getDataWatcherFunction.call(entity)
            ?: throw NoSuchMethodException("Entity#getDataWatcher() 메소드를 찾을 수 없습니다.")
    }

    private fun toNonDefaultValue(dataWatcher: Any): Any {
        return nonDefaultValueFunction?.invoke(dataWatcher)
            ?: throw NoSuchMethodException("DataWatcher#nonDefaultValue() 메소드를 찾을 수 없습니다.")
    }

    private fun getDataWatcher(entity: Any): Any {
        val watcher = getDataWatcherSingle(entity)
        return if (listMetadata) toNonDefaultValue(watcher) else watcher
    }

    override fun createMetaDataPacket(entity: Any): Any {
        val entityId = getId(entity)
        return if (listMetadata) entityMetadataPacket.newInstance(entityId, getDataWatcher(entity))
        else entityMetadataPacket.newInstance(entityId, getDataWatcher(entity), true)
    }

    override fun getId(entity: Any): Int {
        return getIdFunction.call(entity) as? Int ?: throw NoSuchMethodException("Entity#getId() 메소드를 찾을 수 없습니다.")
    }

    override fun setCustomName(name: String, entity: Any) {
        setCustomNameFunction.call(entity, componentWrapper.wrap("{\"text\":\"$name\"}").getUnwrappedInstance())
    }

    override fun setCustomName(name: BaseComponent, entity: Any) {
        setCustomNameFunction.call(entity, componentWrapper.wrap(ComponentSerializer.toString(name)).getUnwrappedInstance())
    }

    override fun setCustomNameVisible(visible: Boolean, entity: Any) {
        setCustomNameVisibleFunction.call(entity, visible)
    }

    override fun setLocation(location: Location, entity: Any) {
        setLocationFunction.call(entity, location.x, location.y, location.z, location.yaw, location.pitch)
    }

    override fun setInvisible(invisible: Boolean, entity: Any) {
        setInvisibleFunction.call(entity, invisible)
    }

    override fun setGlowing(glowing: Boolean, entity: Any) {
        setGlowingTagFunction.call(entity, glowing)
    }

    override fun getEnumItemSlot(enumItemSlot: String): Any {
        return enumItemSlotValueOfFunction.call(enumItemSlot.lowercase()) ?: throw NoSuchElementException()
    }

    override fun getEnumItemSlot(enumItemSlot: EquipmentSlot): Any {
        return enumItemSlotValueOfFunction.call(enumItemSlot.name.lowercase()) ?: throw NoSuchElementException()
    }

    override fun createBukkitPair(first: Any, second: Any): Any {
        return pairConstructor.newInstance(first, second)
    }
}