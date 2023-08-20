package kr.hqservice.framework.global.core.component.handler

interface HQAnnotationHandler<A : Annotation> {
    fun setup(instance: Any, annotation: A) {}

    fun teardown(instance: Any, annotation: A) {}
}