package kr.hqservice.framework.global.core.component

import org.koin.core.module.Module

interface HQKoinModule : HQComponent {
    fun getModule(): KoinModule
}

/**
 * Module 어노테이션과의 이름 충돌으로 인해 ksp 가 정상적으로 작동되지 않기 때문에,
 * Module 을 KoinModule 로 참조합니다.
 */
typealias KoinModule = Module