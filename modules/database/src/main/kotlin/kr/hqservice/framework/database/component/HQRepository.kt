package kr.hqservice.framework.database.component

import kr.hqservice.framework.core.component.HQComponent

interface HQRepository<T, ID> : HQComponent {
    suspend fun saveAll()

    suspend fun deleteAll()
}