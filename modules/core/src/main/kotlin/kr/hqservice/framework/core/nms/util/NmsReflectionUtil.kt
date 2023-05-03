package kr.hqservice.framework.core.nms.util

import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.handler.FunctionType
import kr.hqservice.framework.core.nms.handler.VersionHandler
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

interface NmsReflectionUtil {
    fun getVersionName(): String

    fun getVersion(): Version

    fun getCraftBukkitClass(className: String): KClass<*>

    fun getNmsClass(className: String, vararg handlers: VersionHandler): KClass<*>

    fun getFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*>

    fun getStaticFunction(clazz: KClass<*>, functionType: FunctionType, vararg handlers: VersionHandler): KCallable<*>
}

fun NmsReflectionUtil.getFunction(clazz: KClass<*>, name: String, vararg handlers: VersionHandler): KCallable<*> {
    return getFunction(clazz, FunctionType(name), *handlers)
}

fun NmsReflectionUtil.getFunction(clazz: KClass<*>, name: String, returnType: KClass<*>, vararg handlers: VersionHandler): KCallable<*> {
    return getFunction(clazz, FunctionType(name, returnType.createType()), *handlers)
}

fun NmsReflectionUtil.getFunction(clazz: KClass<*>, name: String, params: List<KClass<*>>, vararg handlers: VersionHandler): KCallable<*> {
    return getFunction(clazz, FunctionType(name, null , params), *handlers)
}

fun NmsReflectionUtil.getFunction(clazz: KClass<*>, name: String, returnType: KClass<*>, params: List<KClass<*>>, vararg handlers: VersionHandler): KCallable<*> {
    return getFunction(clazz, FunctionType(name, returnType.createType(), params), *handlers)
}

fun NmsReflectionUtil.getStaticFunction(clazz: KClass<*>, name: String, params: List<KClass<*>>, vararg handlers: VersionHandler): KCallable<*> {
    return getStaticFunction(clazz, FunctionType(name, null, params, true), *handlers.map { it.static() }.toTypedArray())
}

fun NmsReflectionUtil.getStaticFunction(clazz: KClass<*>, name: String, returnType: KClass<*>, params: List<KClass<*>>, vararg handlers: VersionHandler): KCallable<*> {
    return getStaticFunction(clazz, FunctionType(name, returnType.createType(), params, true), *handlers.map { it.static() }.toTypedArray())
}