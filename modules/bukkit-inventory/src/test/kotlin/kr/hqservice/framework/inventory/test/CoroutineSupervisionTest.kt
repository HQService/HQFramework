package kr.hqservice.framework.inventory.test

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class CoroutineSupervisionTest {
    @Test
    fun test(): Unit = runBlocking {
        val parentScope = CoroutineScope(CoroutineName("parent"))
        val job = Job()
        val childScope = CoroutineScope(job)
        parentScope.launch {
            childScope.launch(this.coroutineContext) {
                while(this.isActive) {
                    println("...active")
                    delay(100)
                }
            }
        }
        delay(500)
        job.cancel()
        delay(10000)
    }
}