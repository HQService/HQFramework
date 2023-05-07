package kr.hqservice.framework.core.component.repository

import kr.hqservice.framework.core.component.Component
import kr.hqservice.framework.core.component.HQComponent
import kotlin.reflect.KClass

/**
 * ComponentRegistry 의 Component 의존성 주입에 대하여:
 *
 * ComponentRegistry 는, 컴포넌트들의 초기화와 종료(Bootstrap)를 관리합니다.
 * Component 들의 초기화 절차에 대해서는 후술합니다.
 * 컴포넌트들의 Bootstrap 은, 플러그인의 초기화(setup)와 종료(teardown)에 의존합니다.
 * 또한, 플러그인이 리로드를 명령받았을 때, 종료(teardown) 후 초기화(setup)됩니다.
 *
 * Component 는 목적에 따른 다양한 종류를 가지며
 * ComponentHandler 를 통하여 컴포넌트 종류를 대응합니다.
 * Component 들의 처리에 대해서는 후술합니다.
 *
 *
 * 컴포넌트의 초기화 절차:
 *
 * 1.
 * 먼저 Component 의 생성자들의 타입 정보에 대하여 가져옵니다. 이때, Component 의 생성자가 두개 이상 존재할 경우,
 * ConstructorConflictException 이 발생합니다.
 *
 * 2.
 * 타입 정보들에 의거하여, 주입하기에 알맞은 인스턴스들을 찾습니다
 * 주입하기에 알맞은 인스턴스들을 찾지 못하였을 경우, 초기화를 건너뛴 뒤 제일 후순위로 미뤄지게 됩니다.
 *
 * 3.
 * 다른 컴포넌트들의 초기화가 끝났을 경우에도 주입하기에 알맞은 인스턴스들을 찾지 못하였을 경우,
 * NoBeanDefinitionsFoundException 을 발생시킵니다.
 *
 *
 * 컴포넌트의 처리:
 *
 * @see Component annotation 을 통해 인식된 컴포넌트는, 1개 이상의 목적을 가질 수 있습니다.
 * 1개 이상의 목적을 지닌 컴포넌트는, 컴포넌트 의존관계에 따른 컴포넌트의 초기화가 진행된 후
 * 구현된 인터페이스에 대응하는 ComponentHandler 가 존재할 경우, 1개 이상의 목적을 완수하게 됩니다.
 */
interface ComponentRepository {
    fun setup()

    fun teardown()

    fun <T : HQComponent> getComponent(key: KClass<T>): T
}