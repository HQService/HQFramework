package kr.hqservice.framework.nms.legacy.wrapper.reflect

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.legacy.wrapper.getFunction
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.jvmErasure

@Component
@Singleton(binds = [NmsReflectionWrapper::class, LegacyNmsReflectionWrapper::class])
class LegacyNmsReflectionWrapperImpl(
    server: Server,
    config: HQYamlConfiguration
) : LegacyNmsReflectionWrapper, HQSimpleComponent {
    private val classMap = ConcurrentHashMap<String, KClass<*>>()
    private val callableMap = ConcurrentHashMap<String, KCallable<*>>()
    private val forgeSupport = config.getBoolean("forge-support")
    private val versionClassName: String = server.javaClass.`package`.name.split(".")[3]
    private val versionName: String = server.bukkitVersion.split("-")[0]
    private val majorVersion = versionName.split(".")[1].toInt()
    private val minorVersion = try {
        versionName.split(".")[2].toInt()
    } catch (e: Exception) {
        0
    }
    private val version = Version.valueOf("V_$majorVersion")
    private val fullVersion = try {
        Version.valueOf("V_${majorVersion}_${minorVersion}")
    } catch (e: Exception) {
        version
    }

    private val craftBukkitClass = "org.bukkit.craftbukkit.$versionClassName."
    private val nmsClass = "net.minecraft.".orLegacy("net.minecraft.server.$versionClassName.")

    private val entityPlayer = getNmsClass("EntityPlayer", Version.V_17.handle("server.level"))
    private val packet = getNmsClass("Packet", Version.V_17.handle("network.protocol"))
    private val craftPlayer = getCraftBukkitClass("entity.CraftPlayer")
    private val playerConnection = getNmsClass("PlayerConnection", Version.V_17.handle("server.network"))

    private val connection by lazy { getField(entityPlayer, "connection",
        Version.V_17.handle("b"),
        Version.V_20.handle("c"),
        Version.V_17_FORGE.handle("f_8906_")
    ) }
    
    private val getHandle by lazy { getFunction(craftPlayer, "getHandle") }
    private val sendPacket by lazy {
        getFunction(playerConnection, "sendPacket", listOf(packet),
            Version.V_18.handleFunction("a") { setParameterClasses(packet) },
            Version.V_20_2.handleFunction("b") { setParameterClasses(packet) },
            Version.V_17_FORGE.handleFunction("m_141995_") { setParameterClasses(packet) },
            Version.V_19_FORGE.handleFunction("m_9829_") { setParameterClasses(packet) },
            Version.V_20_2_FORGE.handleFunction("m_141995_") { setParameterClasses(packet) }
        )
    }

    private val craftServer = getCraftBukkitClass("CraftServer")
    private val getServer by lazy { getFunction(craftServer, "getServer") }

    override fun getNmsPlayerClass(): KClass<*> {
        return entityPlayer
    }

    override fun getVersionName(): String {
        return versionClassName
    }

    override fun getVersion(): Version {
        return version
    }

    override fun getFullVersion(): Version {
        return fullVersion
    }

    override fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*> {
        var name = className
        return classMap.computeIfAbsent(className) {
            getNmsClass(sortHandlers(*handlers)
                .firstOrNull { it.getVersion().support(version, minorVersion) }?.apply {
                    name = if (isChangedName()) "" else ".$name"
                }?.getName()?.run { "$this$name" }
                ?: name)
        }
    }

    private fun getNmsClass(name: String): KClass<*> {
        try {
            return Class.forName("$nmsClass$name").kotlin
        } catch (_: ClassNotFoundException) {
            throw IllegalArgumentException("not found class '$nmsClass$name'")
        }
    }

    override fun getCraftBukkitClass(className: String): KClass<*> {
        try {
            return Class.forName("$craftBukkitClass$className").kotlin
        } catch (_: ClassNotFoundException) {
            throw IllegalArgumentException("not found class '$craftBukkitClass$className'")
        }
    }

    override fun getFunction(
        clazz: KClass<*>,
        functionType: FunctionType,
        vararg handlers: VersionHandler,
    ): KCallable<*> {
        return getFunction(clazz, clazz.functions, functionType, *handlers)
    }

    override fun getStaticFunction(
        clazz: KClass<*>,
        functionType: FunctionType,
        vararg handlers: VersionHandler,
    ): KCallable<*> {
        return getFunction(clazz, clazz.staticFunctions, functionType, *handlers)
    }

    override fun getEntityPlayer(player: Player): Any {
        return getHandle.call(player) ?: throw IllegalArgumentException()
    }

    override fun getNmsServer(server: Server): Any {
        return getServer.call(server) ?: throw IllegalArgumentException()
    }

    override suspend fun sendPacket(player: Player, vararg virtual: Virtual) {
        val handle = getHandle.call(player)
        val connection = connection.call(handle)
        if (connection != null)
            virtual.forEach {
                it.createVirtualMessage()?.also { virtual ->
                    virtual.send { packet ->
                        sendPacket.call(connection, packet)
                        if (it is VirtualContainer) player.updateInventory()
                    }
                }
            }
    }

    override suspend fun sendPacket(players: List<Player>, vararg virtual: Virtual) {
        virtual.forEach {
            it.createVirtualMessage()?.also { virtual ->
                players.forEach { player ->
                    val handle = getHandle.call(player)
                    val connection = connection.call(handle)
                    if (connection != null) virtual.send { packet ->
                        sendPacket.call(connection, packet)
                    }
                }
            }
        }
    }

    override suspend fun sendPacket(players: List<Player>, virtualEntity: AbstractVirtualEntity, switchState: Boolean) {
        virtualEntity.createVirtualMessage(switchState)?.also { virtual ->
            players.forEach { player ->
                val handle = getHandle.call(player)
                val connection = connection.call(handle)
                if (connection != null) virtual.send { packet ->
                    sendPacket.call(connection, packet)
                }
            }
        }
    }

    override fun getField(clazz: KClass<*>, fieldType: KClass<*>): KCallable<*> {
        return clazz.memberProperties.firstOrNull {
            it.returnType.jvmErasure.qualifiedName == fieldType.qualifiedName
        } ?: throw IllegalArgumentException()
    }

    override fun getField(clazz: KClass<*>, fieldName: String, vararg handlers: VersionHandler): KCallable<*> {
        val type = sortHandlers(*handlers)
            .firstOrNull { it.getVersion().support(version, minorVersion) }?.getName() ?: fieldName
        return clazz.memberProperties.firstOrNull {
            it.name == type
        } ?: throw IllegalArgumentException()
    }

    override fun getStaticField(
        clazz: KClass<*>,
        staticFieldName: String,
        vararg handlers: VersionHandler
    ): KCallable<*> {
        val type = sortHandlers(*handlers)
            .firstOrNull { it.getVersion().support(version, minorVersion) }?.getName() ?: staticFieldName
        return clazz.staticProperties.firstOrNull {
            it.name == type
        } ?: throw IllegalArgumentException()
    }

    private fun getFunction(
        clazz: KClass<*>,
        functions: Collection<KCallable<*>>,
        functionType: FunctionType,
        vararg handlers: VersionHandler,
    ): KCallable<*> {
        val key = "${clazz.simpleName}#" + functionType.getName()
        return if (callableMap.contains(key)) callableMap[key]!!
        else {
            val type = sortHandlers(*handlers)
                .filter { it.getVersion().support(version, minorVersion) }
                .run {
                    if (isEmpty()) null
                    else maxBy { it.getVersion().ordinal }
                } ?: CallableVersionHandler(version, functionType)

            try {
                functions.first { callable -> type.isMatched(clazz, callable) }
            } catch (e: Exception) {
                throw NoSuchElementException("${clazz.simpleName}.${type.getName()} 메소드를 찾을 수 업습니다.\n", e)
            }
        }
    }

    private fun sortHandlers(vararg handlers: VersionHandler): List<VersionHandler> {
        return handlers
            .filter {
                if (forgeSupport) it.getVersion().name.endsWith("_FORGE")
                else !it.getVersion().name.endsWith("_FORGE")
            }
            .ifEmpty { handlers.toList() }
            .sortedByDescending { it.getVersion().ordinal }
    }

    private fun String.orLegacy(legacyName: String): String {
        return if (majorVersion >= 17) this else legacyName
    }
}