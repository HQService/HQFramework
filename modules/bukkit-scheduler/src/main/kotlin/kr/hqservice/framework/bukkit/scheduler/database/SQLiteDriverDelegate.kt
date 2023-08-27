package kr.hqservice.framework.bukkit.scheduler.database

import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.sql.ResultSet

class SQLiteDriverDelegate : StdJDBCDelegate() {
    override fun getObjectFromBlob(rs: ResultSet, colName: String?): Any? {
        var obj: Any? = null

        val blobLocator = rs.getBytes(colName)
        if (blobLocator != null && blobLocator.isNotEmpty()) {
            val binaryInput = ByteArrayInputStream(blobLocator)
            if (binaryInput.available() == 0) {
                //do nothing
            } else {
                obj = ObjectInputStream(binaryInput).use { objectInputStream ->
                    objectInputStream.readObject()
                }
            }
        }
        return obj
    }

    override fun getJobDataFromBlob(rs: ResultSet, colName: String?): Any? {
        if (canUseProperties()) {
            val blobLocator = rs.getBytes(colName)
            return ByteArrayInputStream(blobLocator)
        }

        return getObjectFromBlob(rs, colName)
    }
}