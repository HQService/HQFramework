package kr.hqservice.framework.bukkit.core.component.registry

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PluginDepend(val plugins: Array<String>)