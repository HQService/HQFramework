package kr.hqservice.framework.global.core.component.error

import kotlin.reflect.KClass

class NoBeanDefinitionsFoundException(val classes: List<KClass<*>>) : Exception(
    "No definition found for classes: " +
            classes.map { "\n$it" }.toString().replace("[", "").replace("]", "") + "\n" +
            ""
)