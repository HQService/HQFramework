package kr.hqservice.framework.command.test

import kr.hqservice.framework.command.component.CommandExecutor
import kr.hqservice.framework.global.core.extension.print
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredFunctions

class HQCommandExecutorTest {
    @Test
    fun test() {
        this::class.declaredFunctions.forEach {
            it.print("functions")
            it.parameters.forEach {
                it.print("params: ")
            }
        }
    }

    @CommandExecutor("label")
    fun execute(player: Player, string: String) {

    }
}