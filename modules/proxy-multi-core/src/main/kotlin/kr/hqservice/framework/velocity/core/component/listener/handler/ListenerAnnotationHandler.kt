package kr.hqservice.framework.velocity.core.component.listener.handler

import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.global.core.component.handler.HQAnnotationHandler
import kr.hqservice.framework.velocity.core.component.listener.Listener
import kr.hqservice.framework.velocity.core.util.PluginScopeFinder

@AnnotationHandler
class ListenerAnnotationHandler : HQAnnotationHandler<Listener> {
    override fun setup(instance: Any, annotation: Listener) {
        val plugin = PluginScopeFinder.get(instance::class)
        plugin.getProxyServer().eventManager.register(plugin, instance)
    }
}