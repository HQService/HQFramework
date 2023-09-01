package kr.hqservice.framework.bukkit.core.extension

import kr.hqservice.framework.netty.api.NettyPlayer
import kr.hqservice.framework.netty.extension.sendMessage
import org.bukkit.command.CommandSender

fun CommandSender.sendColorizedMessage(string: String) {
    this.sendMessage(string.colorize())
}

fun NettyPlayer.sendColorizedMessage(string: String, logging: Boolean = true) {
    this.sendMessage(string.colorize(), logging)
}