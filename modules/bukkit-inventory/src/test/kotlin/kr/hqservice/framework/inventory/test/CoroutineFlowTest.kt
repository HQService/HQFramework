package kr.hqservice.framework.inventory.test

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

class CoroutineFlowTest {

    private val stateFlow = MutableStateFlow(100)

    @Test
    fun test() = runTest {
        val callback = object : CallbackTestListener {
            override suspend fun onCallback(value: Int) {
                println("callback $value")
                stateFlow.value = value
            }
        }
        launch {
            stateFlow.collect {
                println("Update data $it")
            }
        }
        launch {
            stateFlow.collect {
                println("Update data 2 $it")
            }
        }
        delay(1000)
        callback.onCallback(2)
        delay(1000)
        callback.onCallback(4)
        delay(1000)

        repeat(10) {
            callback.onCallback(it)
        }

        delay(1000)
    }

    interface CallbackTestListener {

        suspend fun onCallback(value: Int)
    }

    @Test
    fun scopeTest() = runTest {
        CoroutineScope(Job() + CoroutineName("nameA")).launch {
            TestCoroutineClass().invoke()
        }
    }

    class TestCoroutineClass : CoroutineScope {
        private val job = Job()

        override val coroutineContext: CoroutineContext
            get() = CoroutineName("nameB") + Dispatchers.IO + job

        suspend fun invoke() = coroutineScope {
            this@coroutineScope.launch {
                this.coroutineContext[CoroutineName.Key]?.name.print("name: ") // nameA
            }
        }
    }
}