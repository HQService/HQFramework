package kr.hqservice.framework.database.exception

import javax.sql.DataSource

class DataSourceClosedException(val dataSource: DataSource) : Throwable("DataSource already closed.")