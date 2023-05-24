package kr.hqservice.framework.database.test

import be.seeseemelk.mockbukkit.MockBukkit
import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.core.HQPlugin
import kr.hqservice.framework.coroutine.component.handler.CoroutineScopeComponentHandler
import kr.hqservice.framework.global.core.component.registry.MutableNamedProvider
import kr.hqservice.framework.global.core.component.registry.QualifierProvider
import kr.hqservice.framework.global.core.extension.print
import kr.hqservice.framework.test.HQFrameworkMock
import kr.hqservice.framework.test.Isolated
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestInstance
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RepositoryTest : KoinComponent {
    private companion object {
        lateinit var testContext: String
    }

    private val repository: TestRepository by inject()

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        testContext = testInfo.tags.first().print("testContext: ")
        MockBukkit.mock()
        val searchScope = listOf(CoroutineScopeComponentHandler::class.java)
        HQFrameworkMock.mock("RepositoryTest", searchScope)
    }

    @AfterEach
    fun teardown() {
        HQFrameworkMock.unmock()
        MockBukkit.unmock()
    }

    @Isolated("RepositoryTest")
    @QualifierProvider("data-source-type")
    class RepositoryQualifierProvider(private val hqPlugin: HQPlugin) : MutableNamedProvider {
        override fun provideQualifier(): String {
            return testContext
        }
    }

    @Tag("sqlite")
    @Test
    fun sqlite_test(): Unit = runBlocking {
        startTest()
    }

    @Tag("mysql")
    @Test
    fun mysql_test(): Unit = runBlocking {
        startTest()
    }

    private suspend fun startTest() = runBlocking {
        val uuid = UUID.randomUUID()
        val entity = repository.create(uuid) {
            this.testField = 1
        }
        entity.testField.print("testField: ")
        repository.update(entity) {
            this.testField = 2
        }
        entity.testField.print("testField: ")
        val updated = repository.updateById(uuid) {
            this.testField = 3
        }
        updated.testField.print("testField: ")
        repository.delete(uuid)
        assert(repository.find(uuid) == null)
        updated.testField.print("testField: ")
        repository.create(uuid) {
            testField = 1
        }
        repository.count().print("count: ")
    }
}