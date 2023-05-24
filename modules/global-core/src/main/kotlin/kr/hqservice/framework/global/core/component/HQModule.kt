package kr.hqservice.framework.global.core.component

interface HQModule : HQComponent {
    fun onEnable()

    fun onDisable()
}