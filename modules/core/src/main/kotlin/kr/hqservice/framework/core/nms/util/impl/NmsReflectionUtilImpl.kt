package kr.hqservice.framework.core.nms.util.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.FunctionType
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kr.hqservice.framework.core.nms.handler.impl.CallableVersionHandler
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import org.bukkit.Server
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.staticFunctions

@Component
@HQSingleton(binds = [NmsReflectionUtil::class])
class NmsReflectionUtilImpl(
    server: Server,
) : NmsReflectionUtil, HQService {

    private val classMap = mutableMapOf<String, KClass<*>>()
    private val callableMap = mutableMapOf<String, KCallable<*>>()

    private val versionName: String = server.javaClass.`package`.name.split(".")[3]
    private val majorVersion = versionName.substring(1).split("_")[1].toInt()
    private val version = Version.valueOf("V_$majorVersion")

    private val craftBukkitClass = "org.bukkit.craftbukkit.$versionName."
    private val nmsClass = "net.minecraft.".orLegacy("net.minecraft.server.$versionName.")

    override fun getVersionName(): String {
        return versionName
    }

    override fun getVersion(): Version {
        return version
    }

    override fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*> {
        return classMap.computeIfAbsent(className) { name ->
            getNmsClass(handlers.sortedByDescending { it.getVersion().ordinal }
                .firstOrNull { it.getVersion().support(version) }?.getName()?.run { "$this.$name" }
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