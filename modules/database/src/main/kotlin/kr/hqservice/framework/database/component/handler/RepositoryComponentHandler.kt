package kr.hqservice.framework.database.component.handler

import kotlinx.coroutines.runBlocking
import kr.hqservice.framework.core.component.handler.ComponentHandler
import kr.hqservice.framework.core.component.handler.HQComponentHandler
import kr.hqservice.framework.database.component.HQRepository
import org.koin.core.component.KoinComponent

@ComponentHandler
class RepositoryComponentHandler : HQComponentHandler<HQRepository<*, *>>, KoinComponent {
    override fun setup(element: HQRepository<*, *>) {}

    override fun teardown(element: HQRepository<*, *>) {
        runBlocking {
            element.saveAll()
        }
    }
}