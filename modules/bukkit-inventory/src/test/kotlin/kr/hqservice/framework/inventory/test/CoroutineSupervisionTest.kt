package kr.hqservice.framework.inventory.test

import kotlinx.coroutines.*
import kr.hqservice.framework.bukkit.core.coroutine.extension.childrenAll
import kr.hqservice.framework.bukkit.core.coroutine.extension.coroutineContext
import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test

class CoroutineSupervisionTest {
    @Test
    fun test(): Unit = runBlocking {
        val job = SupervisorJob()
        val parentScope = CoroutineScope(CoroutineName("parent") + job)
        parentScope.launch {
            launch {
                launch(CoroutineName("child1")) {
                    while(this.isActive) {
                        println("...active1")
                        delay(100)
                    }
                }
                launch {
                    launch(CoroutineName("child2")) {
                        while(this.isActive) {
                            println("...active2")
                            delay(50)
                        }
                    }
                    launch(CoroutineName("child3")) {
                        while(this.isActive) {
                            println("...active3")
                            delay(80)
                        }
                    }
                }
            }
            delay(400)
        }
        delay(500)

        job.childrenAll.forEach {
            it.coroutineContext[CoroutineName].print("coroutineName: ")
        }

        job.cancel()
        delay(10000)
    }
}