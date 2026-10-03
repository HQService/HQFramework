package kr.hqservice.framework.database.repository.player.event

import java.sql.SQLNonTransientConnectionException
import java.sql.SQLTransientConnectionException

enum class SaveFailureHint(val summary: String, val action: String) {
    COLUMN_TOO_SMALL("DB 컬럼 크기 부족", "해당 컬럼을 LONGBLOB 또는 LONGTEXT 로 ALTER 하세요"),
    CONNECTION("DB 에 연결할 수 없음", "DB 서버, 네트워크, 커넥션 풀 상태를 확인하세요"),
    MISSING_SCHEMA("테이블 또는 컬럼이 없음", "기동 로그에 출력된 마이그레이션 SQL 을 실행하세요"),
    DISK_OR_PERMISSION("디스크 공간 또는 계정 권한 문제", "DB 디스크 여유 공간과 계정 권한을 확인하세요"),
    UNKNOWN("원인 미분류", "서버 로그의 스택 트레이스를 확인하세요");

    companion object {
        fun of(cause: Throwable): SaveFailureHint {
            var current: Throwable? = cause
            val seen = mutableSetOf<Throwable>()
            while (current != null && seen.add(current)) {
                val message = current.message?.lowercase() ?: ""
                when {
                    message.contains("data too long") || message.contains("data truncation") -> return COLUMN_TOO_SMALL
                    message.contains("doesn't exist") || message.contains("unknown column") || message.contains("no such table") || message.contains("no such column") -> return MISSING_SCHEMA
                    message.contains("disk full") || message.contains("no space") || message.contains("access denied") || message.contains("permission denied") || message.contains("read-only") -> return DISK_OR_PERMISSION
                    current is SQLTransientConnectionException || current is SQLNonTransientConnectionException || current is java.net.ConnectException || current is java.net.SocketTimeoutException -> return CONNECTION
                    message.contains("communications link") || message.contains("connection refused") || message.contains("connection reset") || message.contains("connection is not available") || message.contains("timed out") -> return CONNECTION
                }
                current = current.cause
            }
            return UNKNOWN
        }
    }
}
