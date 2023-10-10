package kr.hqservice.framework.bungee.core.extension

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent

fun BaseComponent.newTextComponent(): TextComponent {
    val extra = extra
    val newComponent = TextComponent()
    newComponent.hoverEvent = hoverEvent
    newComponent.clickEvent = clickEvent

    if (extra != null && extra.isNotEmpty()) {
        extra.forEach {
            val child = TextComponent()
            val legacy = it.toLegacyText()
            TextComponent.fromLegacyText(legacy).forEach { newText ->
                child.addExtra(newText)
            }
            child.hoverEvent = it.hoverEvent
            child.clickEvent = it.clickEvent
            newComponent.addExtra(child)
        }
    } else {
        val child = TextComponent()
        val legacy = toLegacyText()
        TextComponent.fromLegacyText(legacy).forEach { newText ->
            child.addExtra(newText)
        }
        newComponent.addExtra(child)
    }
    return newComponent
}