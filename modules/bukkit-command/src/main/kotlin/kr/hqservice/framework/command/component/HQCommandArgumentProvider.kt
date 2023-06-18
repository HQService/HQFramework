package kr.hqservice.framework.command.component

import kr.hqservice.framework.global.core.component.HQComponent
import org.bukkit.Location
import org.bukkit.command.CommandSender

sealed interface CommandArgumentProvider<T> : HQComponent

interface HQCommandArgumentProvider<T> : CommandArgumentProvider<T> {
    /**
     * 명령어의 결과를 반환합니다.
     * 만약 명령어 인자에 알맞은 인자가 오지 않았을 경우, false 를 리턴합니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param string 입력 인자
     *
     * @return 인자 입력 결과
     */
    fun getResult(context: CommandContext, string: String?): Boolean

    /**
     * getResult 에서 false 를 반환받았을 때 반환될 메시지입니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param string 입력 인자
     * @param argumentLabel @ArgumentLabel 을 통한 입력 인자 설명
     *
     * @return getResult 에서 false 를 반환받았을 때 명령어 입력 주체 에게 출력될 메시지
     */
    fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String? = null): String?

    /**
     * 커맨드 인자 문자열을 통하여 T 타입으로 캐스팅합니다.
     *
     * @param string 커맨드 인자 문자열
     *
     * @return 인자에 맞게끔 캐스팅 된 후 반환
     */
    fun cast(context: CommandContext, string: String): T

    /**
     * 명령어 인자 자동입력을 반환합니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param location 명령어 입력 위치
     * @param argumentLabel @ArgumentLabel 을 통한 입력 인자 설명
     *
     * @return 자동완성을 추천할 문자열들
     */
    fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String? = null): List<String>
}

interface HQSuspendCommandArgumentProvider<T> : CommandArgumentProvider<T> {
    /**
     * 명령어의 결과를 반환합니다.
     * 만약 명령어 인자에 알맞은 인자가 오지 않았을 경우, false 를 리턴합니다. 이 함수는 suspend 함수 입니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param string 입력 인자
     *
     * @return 인자 입력 결과
     */
    suspend fun getResult(context: CommandContext, string: String?): Boolean

    /**
     * getResult 에서 false 를 반환받았을 때 반환될 메시지입니다. 이 함수는 suspend 함수 입니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param string 입력 인자
     * @param argumentLabel @ArgumentLabel 을 통한 입력 인자 설명
     *
     * @return getResult 에서 false 를 반환받았을 때 명령어 입력 주체 에게 출력될 메시지
     */
    suspend fun getFailureMessage(context: CommandContext, string: String?, argumentLabel: String? = null): String?

    /**
     * 커맨드 인자 문자열을 통하여 T 타입으로 캐스팅합니다. 이 함수는 suspend 함수 입니다.
     *
     * @param string 커맨드 인자 문자열
     *
     * @return 인자에 맞게끔 캐스팅 된 후 반환
     */
    suspend fun cast(context: CommandContext, string: String): T

    /**
     * 명령어 인자 자동입력을 반환합니다. 이 함수는 suspend 함수 입니다.
     *
     * @param context 이 메소드 실행 커맨드 context
     * @param location 명령어 입력 위치
     * @param argumentLabel @ArgumentLabel 을 통한 입력 인자 설명
     *
     * @return 자동완성을 추천할 문자열들
     */
    suspend fun getTabComplete(context: CommandContext, location: Location?, argumentLabel: String? = null): List<String>
}