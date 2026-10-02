package kr.hqservice.framework.database.exception

import javax.sql.DataSource

class DataSourceClosedException(val dataSource: DataSource) : RuntimeException("DataSource already closed.")