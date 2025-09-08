package kr.hqservice.framework.nms.service.chat

import kr.hqservice.framework.nms.service.NmsService
import kr.hqservice.framework.nms.wrapper.chat.BaseComponentWrapper
import net.kyori.adventure.text.Component

interface NmsBaseComponentService : NmsService<String, BaseComponentWrapper> {
    fun wrapFromJson(json: String): BaseComponentWrapper

    fun wrapFromAdventure(adventure: Component): BaseComponentWrapper
}