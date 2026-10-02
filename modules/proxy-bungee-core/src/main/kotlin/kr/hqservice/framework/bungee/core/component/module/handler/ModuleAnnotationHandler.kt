package kr.hqservice.framework.bungee.core.component.module.handler

import kr.hqservice.framework.bungee.core.component.module.Module
import kr.hqservice.framework.bungee.core.component.module.Setup
import kr.hqservice.framework.bungee.core.component.module.Teardown
import kr.hqservice.framework.global.core.component.handler.AnnotationHandler
import kr.hqservice.framework.proxy.core.component.module.handler.ProxyModuleAnnotationHandler

@AnnotationHandler
class ModuleAnnotationHandler : ProxyModuleAnnotationHandler<Module>(Setup::class, Teardown::class)