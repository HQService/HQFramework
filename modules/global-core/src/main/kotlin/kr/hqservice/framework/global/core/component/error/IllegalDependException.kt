package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class IllegalDependException(classes: Collection<KClass<*>>) : Exception("illegal dependencies found: $classes")