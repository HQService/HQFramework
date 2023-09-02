package kr.hqservice.framework.view

import kotlinx.coroutines.Job
import kr.hqservice.framework.view.state.State
import kr.hqservice.framework.view.state.impl.MutableStateFlowState

abstract class ViewModel : LifecycleOwner {
    protected val lifecycleJob = Job()

    protected fun <T> state(data: T): State<T> {
        return MutableStateFlowState(data)
    }

    override fun dispose() {
        lifecycleJob.cancel()
    }
}