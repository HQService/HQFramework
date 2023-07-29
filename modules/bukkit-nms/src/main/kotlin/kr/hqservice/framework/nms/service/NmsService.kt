package kr.hqservice.framework.nms.service

import kr.hqservice.framework.nms.wrapper.NmsWrapper
import kotlin.reflect.KClass

interface NmsService<T, W : NmsWrapper> {
    fun wrap(target: T): W

    fun unwrap(wrapper: W): T

    fun getWrapper(nmsInstance: Any): W {
        throw UnsupportedOperationException("")
    }

    fun getOriginalClass(): KClass<*>

    fun getTargetClass(): KClass<*>
}