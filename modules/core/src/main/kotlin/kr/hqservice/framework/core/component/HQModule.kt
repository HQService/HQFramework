package kr.hqservice.framework.core.component

import kr.hqservice.framework.core.HQPlugin

interface HQModule : HQComponent {
    fun onEnable(plugin: HQPlugin)

    fun onDisable(plugin: HQPlugin)
}