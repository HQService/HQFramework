package kr.hqservice.framework.database.test.datasource

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.database.DatabaseHost
import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.database.component.datasource.MySQLDataSource
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.test.Isolated
import org.koin.core.annotation.Named

@Named("mysql")
@Isolated("RepositoryTest")
@HQSingleton(binds = [HQDataSource::class])
@Component
class TestMySQLDataSource(
    @Named("database") coroutineScope: HQCoroutineScope
) : MySQLDataSource(testDatabaseHost), CoroutineScope by coroutineScope

private val testDatabaseHost = DatabaseHost("localhost", 3306, "test", "testpassword", "test")