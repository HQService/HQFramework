package kr.hqservice.framework

import kr.hqservice.framework.bukkit.HQFrameworkBukkitPlugin
import org.bstats.bukkit.Metrics

class HQFrameworkBukkit : HQFrameworkBukkitPlugin() {
    override fun onPostEnable() {
        Metrics(this, 24492)
    }
}