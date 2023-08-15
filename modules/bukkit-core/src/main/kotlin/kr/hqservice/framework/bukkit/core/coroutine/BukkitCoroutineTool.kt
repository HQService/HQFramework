package kr.hqservice.framework.bukkit.core.coroutine

import kotlinx.coroutines.delay

suspend fun bukkitDelay(tick: Long) {
    delay(tick * 50)
}