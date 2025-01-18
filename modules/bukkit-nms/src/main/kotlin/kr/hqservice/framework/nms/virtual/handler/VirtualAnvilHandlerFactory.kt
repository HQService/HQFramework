package kr.hqservice.framework.nms.virtual.handler

import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

interface VirtualAnvilHandlerFactory {
    fun createListener(player: Player, plugin: Plugin): AnvilDummyListener

    fun createHandler(
        reflectionWrapper: NmsReflectionWrapper,
        textScope: suspend (String) -> Unit,
        confirmScope: suspend (String) -> Boolean,
        buttonScope: suspend (Int, String) -> Boolean,
        otherSlotClickScope: suspend () -> Unit,
        dummyListener: AnvilDummyListener,
        closeScope: suspend (String) -> Unit
    ): VirtualHandler
}