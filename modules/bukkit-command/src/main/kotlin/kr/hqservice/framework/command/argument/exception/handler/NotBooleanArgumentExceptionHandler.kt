package kr.hqservice.framework.command.argument.exception.handler

import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.command.CommandSender

@Component
class NotBooleanArgumentExceptionHandler : CommandArgumentExceptionHandler<ArgumentFeedback.NotBoolean, CommandSender> {
    override fun handle(
        exception: ArgumentFeedback.NotBoolean,
        sender: CommandSender,
        context: CommandContext,
        argument: String?
    ) {
        sender.sendColorizedMessage("&ctrue/false 를 입력해주세요.")
    }
}