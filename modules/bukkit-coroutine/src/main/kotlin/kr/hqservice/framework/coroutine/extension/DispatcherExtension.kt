package kr.hqservice.framework.coroutine.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.coroutine.dispatcher.BukkitMainDispatcher

val Dispatchers.BukkitMain: CoroutineDispatcher get() = BukkitMainDispatcher