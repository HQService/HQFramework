package kr.hqservice.framework.nms.service.chat

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kotlin.reflect.KClass

@Qualifier("base-component")
@Service
class BaseComponentService(
    reflectionWrapper: NmsReflectionWrapper
) : NmsService<String, BaseComponentWrapper> {
    private val componentClass = reflectionWrapper.getNmsClass(
        "IChatBaseComponent",
        Version.V_15.handle("network.chat")
    )
    private val componentSerializerClass = reflectionWrapper.getNmsClass(
        "IChatBaseComponent\$ChatSerializer",
        Version.V_15.handle("network.chat")
    )

    private val serializeFromJsonFunction =
        reflectionWrapper.getFunction(componentSerializerClass,
            FunctionType("b", null, listOf(String::class), true),
            Version.V_19.handleFunction("b") {
                setParameterClasses(String::class)
                static()
            },
            Version.V_20_FORGE.handleFunction("m_130701_") {
                setParameterClasses(String::class)
                static()
            })

    private val serializeFunction = serializeFromJsonFunction

    override fun wrap(target: String): BaseComponentWrapper {
        return BaseComponentWrapper(target,
            serializeFunction.call(target)
                ?: throw UnsupportedOperationException("cannot called ChatSerializer#Serialize(String) function")
        )
    }

    fun wrapFromJson(json: String): BaseComponentWrapper {
        return BaseComponentWrapper(json,
            serializeFromJsonFunction.call(json)
                ?: throw UnsupportedOperationException("cannot called ChatSerializer#fromJson(String) function"))
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