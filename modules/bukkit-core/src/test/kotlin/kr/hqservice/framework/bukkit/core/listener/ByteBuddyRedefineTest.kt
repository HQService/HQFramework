package kr.hqservice.framework.bukkit.core.listener

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.global.core.extension.print
import net.bytebuddy.ByteBuddy
import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.MethodCall
import org.bukkit.event.Listener
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions

class TestListener(): ITestListener {
    val string1: String = UUID.randomUUID().toString()
    val int1: Int = 4
    override suspend fun a() {
        println("$string1, $int1")
    }
}

interface ITestListener {
    suspend fun a()
}

class ByteBuddyRedefineTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            ByteBuddyAgent.install()
        }
    }

    @Test
    fun test() {
        val instance = TestListener().print("original: ") { it.string1 }
        val redefined = ByteBuddy()
            .redefine(instance::class.java)
            .name(instance::class.qualifiedName!! + "\$handlerProxy")
            .implement(Listener::class.java)
            .run {
                try {
                    instance::class.java.getConstructor()
                    this
                } catch (exception: NoSuchMethodException) {
                    this
                        .defineConstructor(Visibility.PUBLIC)
                        .intercept(MethodCall.invokeSuper())
                }
            }
            .make()
            .load(instance::class.java.classLoader, ClassLoadingStrategy.Default.WRAPPER)
            .loaded
        val redefinedInstance: Listener = redefined.getConstructor().newInstance() as Listener
        copyFieldValues(instance, redefinedInstance)
        redefinedInstance::class.declaredFunctions.forEach {
            runBlocking {
                it.callSuspend(redefinedInstance)
            }
        }
    }

    private fun copyFieldValues(source: Any, destination: Any) {
        val sourceClass: Class<*> = source.javaClass
        val destinationClass: Class<*> = destination.javaClass
        for (sourceField in sourceClass.getDeclaredFields()) {
            val destinationField = destinationClass.getDeclaredField(sourceField.name)
            sourceField.setAccessible(true)
            destinationField.setAccessible(true)
            val valueToCopy = sourceField[source]
            destinationField.set(destination, valueToCopy)
        }
    }
}