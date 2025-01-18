package kr.hqservice.framework.global.core.extension

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> List<T>.mapAsync(block: suspend (T) -> R): List<R> = coroutineScope {
    map { async { block(it) } }.awaitAll()
}