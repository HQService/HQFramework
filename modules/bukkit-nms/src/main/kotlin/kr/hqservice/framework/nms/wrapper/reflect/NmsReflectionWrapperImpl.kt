package kr.hqservice.framework.nms.wrapper.reflect

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.getFunction
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.virtual.container.VirtualContainer
import org.bukkit.Server
import org.bukkit.entity.Player
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

@Component
@HQSingleton(binds = [NmsReflectionWrapper::class])
class NmsReflectionWrapperImpl(
    server: Server,
) : NmsReflectionWrapper, HQSimpleComponent {
    private val classMap = mutableMapOf<String, KClass<*>>()
    private val callableMap = mutableMapOf<String, KCallable<*>>()

    private val versionClassName: String = server.javaClass.`package`.name.split(".")[3]
    private val versionName: String = server.bukkitVersion.split("-")[0]
    private val majorVersion = /*versionClassName.substring(1)*/versionName.split(".")[1].toInt()
    private val minorVersion = try {
        versionName.split(".")[2].toInt()
    } catch (e: Exception) {
        0
    }
    private val version = Version.valueOf("V_$majorVersion")

    private val craftBukkitClass = "org.bukkit.craftbukkit.$versionClassName."
    private val nmsClass = "net.minecraft.".orLegacy("net.minecraft.server.$versionClassName.")

    private val entityPlayer = getNmsClass("EntityPlayer", Version.V_15.handle("server.level"))
    private val packet = getNmsClass("Packet", Version.V_15.handle("network.protocol"))
    private val craftPlayer = getCraftBukkitClass("entity.CraftPlayer")
    private val playerConnection = getNmsClass("PlayerConnection", Version.V_15.handle("server.network"))

    private val connection by lazy { getField(entityPlayer, "playerConnection", Version.V_17.handle("b")) }
    private val getHandle by lazy { getFunction(craftPlayer, "getHandle") }
    private val sendPacket by lazy {
        getFunction(playerConnection, "sendPacket", listOf(packet), Version.V_18.handleFunction("a") {
            setParameterClasses(packet)
        })
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

    override fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*> {
        var name = className
        return classMap.computeIfAbsent(className) {
            getNmsClass(handlers.sortedByDescending { it.getVersion().ordinal }
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
                        if(it is VirtualContainer) player.updateInventory()
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
                    if(connection != null) virtual.send { packet ->
                        sendPacket.call(connection, packet)
                    }
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
        val type = handlers.sortedByDescending { it.getVersion().ordinal }
            .firstOrNull { it.getVersion().support(version, minorVersion) }?.getName() ?: fieldName
        return clazz.memberProperties.firstOrNull {
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
            val type = handlers.filter { it.getVersion().support(version, minorVersion) }
                .run {
                    if (isEmpty()) null
                    else maxBy { it.getVersion().ordinal }
                } ?: CallableVersionHandler(version, functionType)
            try {
                functions.single { callable -> type.isMatched(clazz, callable) }
            } catch (e: Exception) {
                throw NoSuchElementException("${clazz.simpleName}.${type.getName()} 메소드를 찾을 수 업습니다.\n원본 메서드 ${functionType.getName()}")
            }
        }
    }

    private fun String.orLegacy(legacyName: String): String {
        return if (majorVersion >= 17) this else legacyName
    }
}