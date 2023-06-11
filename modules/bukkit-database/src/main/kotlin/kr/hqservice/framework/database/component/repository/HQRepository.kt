package kr.hqservice.framework.database.component.repository

import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.global.core.component.HQComponent

interface HQRepository : HQComponent {
    fun getDataSource(): HQDataSource
}