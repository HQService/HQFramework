package kr.hqservice.framework.nms.v20_6.service.chat

import kr.hqservice.framework.nms.service.chat.NmsBaseComponentService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass

class NmsBaseComponentServiceImpl : NmsBaseComponentService {
    override fun wrap(target: String): BaseComponentWrapper {
        return BaseComponentWrapper(
            target,
            Component.Serializer.fromJsonLenient(target, RegistryAccess.EMPTY)!!
        )
    }

    fun wrapFromJson(json: String): BaseComponentWrapper {
        return wrap(json)
    }

    override fun unwrap(wrapper: BaseComponentWrapper): String {
        return wrapper.baseString
    }

    override fun getOriginalClass(): KClass<*> {
        return String::class
    }

    override fun getTargetClass(): KClass<*> {
        return Component::class
    }
}