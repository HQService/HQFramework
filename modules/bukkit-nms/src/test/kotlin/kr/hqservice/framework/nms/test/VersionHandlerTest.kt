package kr.hqservice.framework.nms.test

import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class VersionHandlerTest {
    @Test
    fun version_handle_test() {
        val testVersion = "v1_19_2"
        val majorVersion = testVersion.substring(1).split("_")[1].toInt()
        val minorVersion = testVersion.substring(1).split("_")[2].toInt()

        majorVersion.print("major: ")
        minorVersion.print("minor: ")
    }

}