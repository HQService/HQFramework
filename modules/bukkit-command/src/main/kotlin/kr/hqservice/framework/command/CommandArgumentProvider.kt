package kr.hqservice.framework.command

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.Location

/**
 * IO Dispatcher 에서 작동됩니다.
 */
interface CommandArgumentProvider<T> : HQComponent {
    /**
     * 커맨드 인자 문자열을 통하여 T 타입으로 캐스팅합니다.
     *
     * @param argument 커맨드 인자 문자열
     *
     * @return 인자에 맞게끔 캐스팅 된 후 반환
     */
    suspend fun cast(context: CommandContext, argument: String?): T

    /**
     * 명령어 인자 자동입력을 반환합니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param location 명령어 입력 위치
     * @param argumentLabel @ArgumentLabel 을 통한 입력 인자 설명
     *
     * @return 자동완성을 추천할 문자열들
     */
    suspend fun getTabComplete(context: CommandContext, location: Location?): List<String>
}