package kr.hqservice.framework.nms.service.chat

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper

interface NmsBaseComponentService : NmsService<String, BaseComponentWrapper> {
    fun wrapFromJson(json: String): BaseComponentWrapper
}