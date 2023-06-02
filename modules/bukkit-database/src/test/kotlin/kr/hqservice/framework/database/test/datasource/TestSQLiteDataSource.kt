package kr.hqservice.framework.database.test.datasource

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQSingleton
import kr.hqservice.framework.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.database.component.HQDataSource
import kr.hqservice.framework.database.component.SQLiteDataSource
import kr.hqservice.framework.test.Isolated
import org.koin.core.annotation.Named

@Named("sqlite")
@Isolated("RepositoryTest")
@HQSingleton(binds = [HQDataSource::class])
@Component
class TestSQLiteDataSource(
    @Named("database") coroutineScope: HQCoroutineScope
) : SQLiteDataSource("test/database/database.db"), CoroutineScope by coroutineScope