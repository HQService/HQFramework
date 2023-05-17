package kr.hqservice.netty.test

import kr.hqservice.framework.netty.packet.server.HandShakePacket
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.full.primaryConstructor

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NettyTest {

    @Test
    fun constructorTest() {
        val clazz = HandShakePacket::class
        println(clazz.primaryConstructor?.parameters)
        clazz.constructors.forEach {
            println(it.parameters)
        }
    }

}