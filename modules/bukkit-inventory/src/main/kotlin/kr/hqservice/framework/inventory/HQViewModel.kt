package kr.hqservice.framework.inventory

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kr.hqservice.framework.coroutine.extension.BukkitMain
import kr.hqservice.framework.inventory.coroutine.LifecycleOwner
import kr.hqservice.framework.inventory.state.State
import kr.hqservice.framework.inventory.state.impl.MutableStateFlowState
import kotlin.coroutines.CoroutineContext

abstract class HQViewModel : LifecycleOwner {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("HQViewModelCoroutine") + job + Dispatchers.BukkitMain

    protected fun <T : Any> state(data: T): State<T> {
        return MutableStateFlowState(data)
    }

    final override fun dispose() {
        job.cancel()
    }
}