package kr.hqservice.framework.coroutine

import kotlinx.coroutines.delay

suspend fun bukkitDelay(tick: Long) {
    delay(tick * 50)
}