package kr.hqservice.framework.coroutine.dispatcher

import com.google.auto.service.AutoService
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.internal.MainDispatcherFactory

@AutoService(MainDispatcherFactory::class)
@InternalCoroutinesApi
class BukkitMainDispatcherFactory : MainDispatcherFactory {
    override val loadPriority: Int
        get() = 0

    override fun createDispatcher(allFactories: List<MainDispatcherFactory>): MainCoroutineDispatcher {
        return BukkitMainDispatcher
    }
}