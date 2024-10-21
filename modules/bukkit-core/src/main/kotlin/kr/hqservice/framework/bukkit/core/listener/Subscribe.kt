package kr.hqservice.framework.bukkit.core.listener

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subscribe(
    val handleOrder: HandleOrder = HandleOrder.NORMAL,
    val ignoreCancelled: Boolean = false
)
