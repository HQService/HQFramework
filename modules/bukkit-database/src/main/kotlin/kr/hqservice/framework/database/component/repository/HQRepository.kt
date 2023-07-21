package kr.hqservice.framework.database.component.repository

import kr.hqservice.framework.database.component.datasource.HQDataSource
import kr.hqservice.framework.global.core.component.Component
import kr.hqservice.framework.global.core.component.HQComponent

typealias Repository = Component

interface HQRepository : HQComponent {
    val dataSource: HQDataSource
}