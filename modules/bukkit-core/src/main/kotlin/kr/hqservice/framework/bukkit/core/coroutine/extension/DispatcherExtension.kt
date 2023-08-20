package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.bukkit.core.coroutine.dispatcher.BukkitDispatcher

private val bukkitMainDispatcher = BukkitDispatcher(false)
private val bukkitAsyncDispatcher = BukkitDispatcher(true)

val Dispatchers.BukkitMain: CoroutineDispatcher
    get() = bukkitMainDispatcher

val Dispatchers.BukkitAsync: CoroutineDispatcher
    get() = bukkitAsyncDispatcher