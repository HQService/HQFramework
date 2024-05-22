package kr.hqservice.framework.bukkit.core.service.economy.impl

import kr.hqservice.framework.bukkit.core.component.registry.PluginDepend
import kr.hqservice.framework.bukkit.core.service.economy.EconomyService
import kr.hqservice.framework.global.core.component.Service
import net.milkbowl.vault.economy.Economy
import org.bukkit.OfflinePlayer
import org.bukkit.Server

@PluginDepend(["Vault"])
@Service
class EconomyServiceImpl(
    private val server: Server
) : EconomyService {

    private val economy by lazy {
        server.servicesManager.getRegistration(Economy::class.java)!!.provider
    }

    override fun hasBalance(player: OfflinePlayer, money: Double): Boolean {
        return economy.has(player, money)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return economy.getBalance(player)
    }

    override fun deposit(player: OfflinePlayer, money: Double) {
        economy.depositPlayer(player, money)
    }

    override fun withdraw(player: OfflinePlayer, money: Double) {
        economy.withdrawPlayer(player, money)
    }
}