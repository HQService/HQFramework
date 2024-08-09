package kr.hqservice.framework.nms.service.chat

import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Service
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass

@Qualifier("base-component")
@Service
class BaseComponentService : NmsService<String, BaseComponentWrapper> {
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