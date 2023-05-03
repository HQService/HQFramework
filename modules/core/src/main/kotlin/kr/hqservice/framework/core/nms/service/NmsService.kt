package kr.hqservice.framework.core.nms.service

import kr.hqservice.framework.core.nms.wrapper.NmsWrapper

interface NmsService<T, W: NmsWrapper> {
    fun wrap(target: T): W

    fun unwrap(wrapper: W): T
}