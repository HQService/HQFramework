package kr.hqservice.framework.bukkit.core.service.economy

import org.bukkit.OfflinePlayer

interface EconomyService {

    fun hasBalance(player: OfflinePlayer, money: Double): Boolean

    fun getBalance(player: OfflinePlayer): Double

    fun deposit(player: OfflinePlayer, money: Double)

    fun withdraw(player: OfflinePlayer, money: Double)
}