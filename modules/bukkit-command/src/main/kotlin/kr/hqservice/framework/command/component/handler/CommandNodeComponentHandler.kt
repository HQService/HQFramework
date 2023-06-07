package kr.hqservice.framework.command.component.handler

import kr.hqservice.framework.command.component.CommandExecutor
import kr.hqservice.framework.command.component.ParentCommand
import kr.hqservice.framework.command.component.HQCommandNode
import kr.hqservice.framework.command.component.registry.CommandRegistry
import kr.hqservice.framework.global.core.component.handler.ComponentHandler
import kr.hqservice.framework.global.core.component.handler.HQComponentHandler
import kr.hqservice.framework.global.core.component.handler.impl.KoinModuleComponentHandler
import org.bukkit.command.CommandSender
import java.util.logging.Logger
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

@ComponentHandler(depends = [KoinModuleComponentHandler::class])
class CommandNodeComponentHandler(
    private val registry: CommandRegistry,
    private val logger: Logger
) : HQComponentHandler<HQCommandNode> {
    override fun setup(element: HQCommandNode) {
        val parentCommand = element::class.findAnnotation<ParentCommand>() ?: throw IllegalStateException("Command nodes must have parent tree command(s)")

        element::class.memberFunctions.filterIsInstance<KFunction<Unit>>().forEach { function ->
            function.annotations.filterIsInstance<CommandExecutor>().forEach { commandExecutor ->
                if (!function.valueParameters.first().type.jvmErasure.isSubclassOf(CommandSender::class)) {
                    logger.severe("HQCommandExecutor 의 첫번째 인자에는 CommandSender 를 상속받는 클래스가 와야합니다. " +
                            "${element::class.simpleName} Command 에 대한 등록을 건너뜁니다.")
                    return
                }
                element.addExecutor(commandExecutor, function)
            }
        }

        parentCommand.binds.forEach { parentCommand ->
            registry.addNode(parentCommand, element)
        }
    }
}