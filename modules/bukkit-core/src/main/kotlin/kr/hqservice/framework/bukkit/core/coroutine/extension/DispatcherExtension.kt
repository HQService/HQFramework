package kr.hqservice.framework.bukkit.core.coroutine.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kr.hqservice.framework.bukkit.core.coroutine.dispatcher.BukkitDispatcher
import org.bukkit.Location

private val bukkitMainDispatcher = BukkitDispatcher(false, null)
private val bukkitAsyncDispatcher = BukkitDispatcher(true, null)

val Dispatchers.BukkitMain: CoroutineDispatcher
    get() = bukkitMainDispatcher

val Dispatchers.BukkitAsync: CoroutineDispatcher
    get() = bukkitAsyncDispatcher

fun Dispatchers.FoliaRegion(location: Location): CoroutineDispatcher {
    return BukkitDispatcher(false, location)
}

fun Dispatchers.FoliaRegionAsync(location: Location): CoroutineDispatcher {
    return BukkitDispatcher(true, location)
}