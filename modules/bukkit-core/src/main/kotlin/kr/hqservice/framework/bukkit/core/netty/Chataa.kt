package kr.hqservice.framework.bukkit.core.netty

import kr.hqservice.framework.bukkit.core.component.HQListener
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent

@Component
class Chataa : HQListener {
    @EventHandler
    fun a(event: AsyncPlayerChatEvent) {
        event.player.sendMessage(event.message.colorize())
    }
}