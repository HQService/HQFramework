package kr.hqservice.framework.command.argument.exception.handler

import kr.hqservice.framework.bukkit.core.extension.sendColorizedMessage
import kr.hqservice.framework.command.CommandArgumentExceptionHandler
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.command.CommandSender

@Component
class PlayerNotFoundArgumentExceptionHandler : CommandArgumentExceptionHandler<ArgumentFeedback.PlayerNotFound, CommandSender> {
    override fun handle(
        exception: ArgumentFeedback.PlayerNotFound,
        sender: CommandSender,
        context: CommandContext,
        argument: String?
    ) {
        sender.sendColorizedMessage("&c플레이어 $argument 을(를) 찾을 수 없습니다.")
    }
}