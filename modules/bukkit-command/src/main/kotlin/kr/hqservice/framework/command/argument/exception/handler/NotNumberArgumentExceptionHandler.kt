package kr.hqservice.framework.command.argument.exception.handler

import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.command.CommandSender

@Component
class NotNumberArgumentExceptionHandler : CommandArgumentExceptionHandler<ArgumentFeedback.NotNumber, CommandSender> {
    override fun handle(
        exception: ArgumentFeedback.NotNumber,
        sender: CommandSender,
        context: CommandContext,
        argument: String?
    ) {
        sender.sendColorizedMessage("&c올바른 숫자를 입력해 주세요.")
    }
}