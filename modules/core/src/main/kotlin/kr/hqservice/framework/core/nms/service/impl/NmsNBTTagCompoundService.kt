package kr.hqservice.framework.core.nms.service.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.core.nms.Version
import kr.hqservice.framework.core.nms.service.NmsService
import kr.hqservice.framework.core.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.wrapper.impl.NmsNBTTagCompoundWrapper
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.full.primaryConstructor

@Component
@Named("tag")
@HQSingleton(binds = [NmsService::class])
class NmsNBTTagCompoundService : KoinComponent, NmsService<Any?, NmsNBTTagCompoundWrapper> {

    private val reflectionUtil: NmsReflectionUtil by inject()

    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound",
        Version.V_15.handle("nbt"))

    override fun wrap(target: Any?): NmsNBTTagCompoundWrapper {
        return NmsNBTTagCompoundWrapper(target?: nbtTagClass.primaryConstructor?.call() ?: throw IllegalArgumentException())
    }

    override fun unwrap(wrapper: NmsNBTTagCompoundWrapper): Any {
        return wrapper.nbtTag
    }

}
