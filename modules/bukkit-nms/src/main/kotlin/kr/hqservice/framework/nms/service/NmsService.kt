package kr.hqservice.framework.nms.service

import kr.hqservice.framework.nms.wrapper.NmsWrapper

interface NmsService<T, W: NmsWrapper> {
    fun wrap(target: T): W

    fun unwrap(wrapper: W): T
}