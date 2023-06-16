package kr.hqservice.framework.command.component

import org.bukkit.command.CommandSender

abstract class HQCommandRoot(val name: String) : HQCommandTree(name) {
    open fun getFallbackPrefix(): String = "hq"

    /**
     * 명령어를 실행하는데 필요한 권한이 부족한 경우 플레이어에게 권한이 부족하다고 알려주는 메시지를 보냅니다.
     *
     * @param sender 입력한 사람
     * @param requiredPermission 필요한 권한 노드, 필요한 권한이 op 권한일 경우 null
     */
    open fun sendPermissionDeclinedMessage(sender: CommandSender, requiredPermission: String? = null) {
        sender.sendMessage("§c권한이 부족합니다.")
    }
}