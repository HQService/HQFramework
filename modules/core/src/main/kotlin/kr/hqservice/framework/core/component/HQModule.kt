package kr.hqservice.framework.core.component

interface HQModule : HQComponent {
    fun onEnable()

    fun onDisable()
}