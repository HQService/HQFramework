package kr.hqservice.framework.bukkit.core.component

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PluginDepend(val plugins: Array<String>)