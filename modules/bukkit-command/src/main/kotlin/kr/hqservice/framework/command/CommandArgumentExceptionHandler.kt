package kr.hqservice.framework.command

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.command.CommandSender

interface CommandArgumentExceptionHandler<T : Throwable, S : CommandSender> : HQComponent {
    fun handle(exception: T, sender: S, context: CommandContext, argument: String?)
}