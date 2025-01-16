package kr.hqservice.framework.nms.legacy.service.chat

import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.handler.FunctionType
import kr.hqservice.framework.nms.legacy.wrapper.LegacyNmsReflectionWrapper
import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import kotlin.reflect.KClass

class LegacyNmsBaseComponentService(
    reflectionWrapper: LegacyNmsReflectionWrapper
) : NmsBaseComponentService {
    private val componentClass = reflectionWrapper.getNmsClass("IChatBaseComponent",
        Version.V_17.handle("network.chat")
    )
    private val componentSerializerClass = reflectionWrapper.getNmsClass("IChatBaseComponent\$ChatSerializer",
        Version.V_17.handle("network.chat")
    )
    private val serializeFromJsonFunction = reflectionWrapper.getFunction(componentSerializerClass,
        FunctionType("b", null, listOf(String::class), true),
        Version.V_17.handleFunction("b") { // fromJsonLenient ~1.20.2
            setParameterClasses(String::class)
            static()
        },
        Version.V_17_FORGE.handleFunction("m_130714_") { // fromJsonLenient (old: m_130701_)
            setParameterClasses(String::class)
            static()
        }
    )
    private val serializeFunction = serializeFromJsonFunction

    override fun wrap(target: String): BaseComponentWrapper {
        return BaseComponentWrapper(
            target,
            serializeFunction.call(target)
                ?: throw UnsupportedOperationException("cannot called ChatSerializer#Serialize(String) function")
        )
    }

    fun wrapFromJson(json: String): BaseComponentWrapper {
        return BaseComponentWrapper(
            json,
            serializeFromJsonFunction.call(json)
                ?: throw UnsupportedOperationException("cannot called ChatSerializer#fromJson(String) function")
        )
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