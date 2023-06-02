package kr.hqservice.framework.nms.util.impl

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSimpleComponent
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.packet.PacketWrapper
import org.bukkit.Server
import org.bukkit.entity.Player
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.jvmErasure

@Component
@HQSingleton(binds = [NmsReflectionUtil::class])
class NmsReflectionUtilImpl(
    server: Server,
) : NmsReflectionUtil, HQSimpleComponent {
    private val classMap = mutableMapOf<String, KClass<*>>()
    private val callableMap = mutableMapOf<String, KCallable<*>>()

    private val versionName: String = server.javaClass.`package`.name.split(".")[3]
    private val majorVersion = versionName.substring(1).split("_")[1].toInt()
    private val version = Version.valueOf("V_$majorVersion")

    private val craftBukkitClass = "org.bukkit.craftbukkit.$versionName."
    private val nmsClass = "net.minecraft.".orLegacy("net.minecraft.server.$versionName.")

    private val entityPlayer= getNmsClass("EntityPlayer", Version.V_15.handle("server.level"))
    private val craftPlayer = getCraftBukkitClass("entity.CraftPlayer")
    private val playerConnection = getNmsClass("PlayerConnection", Version.V_15.handle("server.network"))

    private val connection by lazy { getFunction(entityPlayer, "playerConnection", Version.V_17.handle("b")) }
    private val getHandle by lazy { getFunction(craftPlayer, "getHandle") }
    private val sendPacket by lazy { getFunction(playerConnection, "sendPacket", Version.V_18.handle("a")) }

    private val craftServer = getCraftBukkitClass("CraftServer")

    private val getServer by lazy { getFunction(craftServer, "getServer") }

    override fun getVersionName(): String {
        return versionName
    }

    override fun getVersion(): Version {
        return version
    }

    override fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*> {
        var name = className
        return classMap.computeIfAbsent(className) {
            getNmsClass(handlers.sortedByDescending { it.getVersion().ordinal }
                .firstOrNull { it.getVersion().support(version) }?.apply {
                    name = if(isChangedName()) "" else ".$name"
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

    override fun getFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*> {
        return getFunction(clazz, clazz.declaredFunctions, functionType, *handlers)
    }

    override fun getStaticFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*> {
        return getFunction(clazz, clazz.staticFunctions, functionType, *handlers)
    }

    override fun getEntityPlayer(player: Player): Any {
        return getHandle.call(player)?: throw IllegalArgumentException()
    }

    override fun getNmsServer(server: Server): Any {
        return getServer.call(server)?: throw IllegalArgumentException()
    }

    override suspend fun sendPacket(player: Player, vararg packetWrapper: PacketWrapper) {
        val handle = getHandle.call(player)
        val connection = connection.call(handle)

        if(connection != null)
            packetWrapper.forEach {
                this.sendPacket.call(connection, it.createPacket()) }
    }

    override fun getField(clazz: KClass<*>, fieldType: KClass<*>): KCallable<*> {
        return clazz.declaredMembers.firstOrNull {
            it.returnType.jvmErasure.qualifiedName == fieldType.qualifiedName
        }?: throw IllegalArgumentException()
    }

    override fun getField(clazz: KClass<*>, fieldName: String, vararg handlers: VersionHandler): KCallable<*> {
        val type = handlers.sortedByDescending { it.getVersion().ordinal }
            .firstOrNull { it.getVersion().support(version) }?.getName()?: fieldName

        return clazz.declaredMembers.firstOrNull {
            it.name == type
        }?: throw IllegalArgumentException()
    }

    private fun getFunction(clazz: KClass<*>, functions: Collection<KCallable<*>>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*> {
        return callableMap.computeIfAbsent("${clazz.simpleName}#"+functionType.getName()) { _ ->
            val type = handlers.sortedByDescending { it.getVersion().ordinal }
                .firstOrNull { it.getVersion().support(version) }?: CallableVersionHandler(version, functionType)
            functions.single { callable -> type.isMatched(clazz, callable) }
        }
    }

    private fun String.orLegacy(legacyName: String): String {
        return if (majorVersion >= 17) this else legacyName
    }
}