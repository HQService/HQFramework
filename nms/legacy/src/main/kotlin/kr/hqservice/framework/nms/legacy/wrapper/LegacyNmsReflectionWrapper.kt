package kr.hqservice.framework.nms.legacy.wrapper

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.handler.VersionHandler
import kr.hqservice.framework.nms.virtual.AbstractVirtualEntity
import kr.hqservice.framework.nms.virtual.Virtual
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.Server
import org.bukkit.entity.Player
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

interface LegacyNmsReflectionWrapper : NmsReflectionWrapper {
    fun getNmsPlayerClass(): KClass<*>

    fun getVersionName(): String

    fun getVersion(): Version

    fun getFullVersion(): Version

    fun getCraftBukkitClass(className: String): KClass<*>

    fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*>

    fun getFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*>

    fun getStaticFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*>

    fun getEntityPlayer(player: Player): Any

    fun getNmsServer(server: Server): Any

    fun getField(clazz: KClass<*>, fieldType: KClass<*>): KCallable<*>

    fun getField(clazz: KClass<*>, fieldName: String, vararg handlers: VersionHandler): KCallable<*>

    fun getStaticField(clazz: KClass<*>, staticFieldName: String, vararg handlers: VersionHandler): KCallable<*>
}

fun LegacyNmsReflectionWrapper.getFunction(clazz: KClass<*>, name: String, vararg handlers: VersionHandler): KCallable<*> {
    return getFunction(clazz, FunctionType(name), *handlers)
}

fun LegacyNmsReflectionWrapper.getFunction(
    clazz: KClass<*>,
    name: String,
    returnType: KClass<*>,
    vararg handlers: VersionHandler
): KCallable<*> {
    return getFunction(clazz, FunctionType(name, returnType.createType()), *handlers)
}

fun LegacyNmsReflectionWrapper.getFunction(
    clazz: KClass<*>,
    name: String,
    params: List<KClass<*>>,
    vararg handlers: VersionHandler
): KCallable<*> {
    return getFunction(clazz, FunctionType(name, null, params), *handlers)
}

fun LegacyNmsReflectionWrapper.getFunction(
    clazz: KClass<*>,
    name: String,
    returnType: KClass<*>,
    params: List<KClass<*>>,
    vararg handlers: VersionHandler
): KCallable<*> {
    return getFunction(clazz, FunctionType(name, returnType.createType(), params), *handlers)
}

fun LegacyNmsReflectionWrapper.getStaticFunction(
    clazz: KClass<*>,
    name: String,
    params: List<KClass<*>>,
    vararg handlers: VersionHandler
): KCallable<*> {
    return getStaticFunction(
        clazz,
        FunctionType(name, null, params, true),
        *handlers.map { it.static() }.toTypedArray()
    )
}

fun LegacyNmsReflectionWrapper.getStaticFunction(
    clazz: KClass<*>,
    name: String,
    returnType: KClass<*>,
    params: List<KClass<*>>,
    vararg handlers: VersionHandler
): KCallable<*> {
    return getStaticFunction(
        clazz,
        FunctionType(name, returnType.createType(), params, true),
        *handlers.map { it.static() }.toTypedArray()
    )
}