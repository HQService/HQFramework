package kr.hqservice.framework.database.component

import kr.hqservice.framework.global.core.component.HQComponent
import org.jetbrains.exposed.dao.Entity

interface HQRepository< ID : Comparable<ID>, T : Entity<ID>> : HQComponent {
    fun getEntityClass(): HQEntityClass<ID, T>

    fun getDataSource(): HQDataSource
}