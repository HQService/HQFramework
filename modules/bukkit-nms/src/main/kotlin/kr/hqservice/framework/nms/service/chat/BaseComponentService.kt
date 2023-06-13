package kr.hqservice.framework.nms.service.chat

import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.nms.util.getFunction
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import org.koin.core.annotation.Named
import kotlin.reflect.KClass

@Component
@Named("base-component")
@HQSingleton(binds = [NmsService::class])
class BaseComponentService(
    reflectionUtil: NmsReflectionUtil
) : NmsService<String, BaseComponentWrapper> {
    private val componentClass = reflectionUtil.getNmsClass("IChatBaseComponent",
        Version.V_15.handle("network.chat"))
    private val componentSerializerClass = reflectionUtil.getNmsClass("IChatBaseComponent\$ChatSerializer",
        Version.V_15.handle("network.chat"))
    private val serializeFunction = reflectionUtil.getFunction(componentSerializerClass, FunctionType("a", null, listOf(String::class), true))

    override fun wrap(target: String): BaseComponentWrapper {
        return BaseComponentWrapper(target, serializeFunction.call("{\"text\": \"§f$target\"}")?: throw UnsupportedOperationException("cannot called ChatSerializer#Serialize(String) function"))
    }

    override fun unwrap(wrapper: BaseComponentWrapper): String {
        return wrapper.baseString
    }

    override fun getOriginalClass(): KClass<*> {
        return String::class
    }

    override fun getTargetClass(): KClass<*> {
        return componentClass
    }
}