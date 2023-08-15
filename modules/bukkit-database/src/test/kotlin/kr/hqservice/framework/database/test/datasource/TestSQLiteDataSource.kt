package kr.hqservice.framework.database.test.datasource

import kotlinx.coroutines.CoroutineScope
import kr.hqservice.framework.bukkit.core.coroutine.component.HQCoroutineScope
import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.database.component.datasource.SQLiteDataSource
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.Qualifier
import kr.hqservice.framework.global.core.component.Singleton
import kr.hqservice.framework.test.Isolated
import org.koin.core.annotation.Named

@Qualifier("sqlite")
@Isolated("RepositoryTest")
@Singleton(binds = [HQDataSource::class])
@Component
class TestSQLiteDataSource(
    @Named("database") coroutineScope: HQCoroutineScope
) : SQLiteDataSource("test/database/database.db"), CoroutineScope by coroutineScope