package kr.hqservice.framework.bukkit.core.extension

import org.bukkit.Sound
import org.bukkit.World

fun World.playSound(sound: Sound, volume: Float = 1f, pitch: Float = 1f) {
    players.forEach { it.playSound(it.location, sound, volume, pitch) }
}

fun World.playSound(sound: String, volume: Float = 1f, pitch: Float = 1f) {
    players.forEach { it.playSound(it.location, sound, volume, pitch) }
}

fun World.sendMessages(vararg messages: String?) {
    players.forEach { messages.filterNotNull().forEach(it::sendColorizedMessage) }
}