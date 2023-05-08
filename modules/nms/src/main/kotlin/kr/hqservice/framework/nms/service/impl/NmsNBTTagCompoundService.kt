package kr.hqservice.framework.nms.service.impl

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQService
import kr.hqservice.framework.core.component.HQSingleton
import kr.hqservice.framework.core.extension.print
import kr.hqservice.framework.nms.Version
import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.util.NmsReflectionUtil
import kr.hqservice.framework.core.nms.wrapper.impl.NmsNBTTagCompoundWrapper
import org.koin.core.annotation.Named
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Component
@Named("tag")
@HQSingleton(binds = [NmsService::class])
class NmsNBTTagCompoundService(
    reflectionUtil: NmsReflectionUtil
) : KoinComponent, NmsService<Any?, NmsNBTTagCompoundWrapper>, HQService {
    private val nbtTagClass = reflectionUtil.getNmsClass("NBTTagCompound",
        Version.V_15.handle("nbt"))

    override fun wrap(target: Any?): NmsNBTTagCompoundWrapper {
        return NmsNBTTagCompoundWrapper(target?: nbtTagClass.createInstance())
    }

    override fun unwrap(wrapper: NmsNBTTagCompoundWrapper): Any {
        return wrapper.nbtTag
    }
}
