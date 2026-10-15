package kr.hqservice.framework.nms.v21.wrapper.reflect

import kr.hqservice.framework.nms.wrapper.NmsReflectionWrapper
import net.minecraft.server.level.ServerPlayer
import org.bukkit.entity.Player

interface NmsEntityPlayerAccessor : NmsReflectionWrapper {
    fun getEntityPlayer(player: Player): ServerPlayer
}
