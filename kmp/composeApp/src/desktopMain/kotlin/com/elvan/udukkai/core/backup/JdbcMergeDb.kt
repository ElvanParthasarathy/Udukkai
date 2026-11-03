package com.elvan.udukkai.core.backup

import java.sql.Connection

/** [MergeDb] over a JDBC SQLite connection (the desktop app and the merge tests). */
class JdbcMergeDb(private val conn: Connection) : MergeDb {
    override fun count(sql: String): Int = conn.createStatement().use { st -> st.executeQuery(sql).use { rs -> if (rs.next()) rs.getInt(1) else 0 } }

    override fun columns(schema: String, table: String): List<String> = conn.createStatement().use { st ->
        st.executeQuery("PRAGMA $schema.table_info(\"$table\")").use { rs ->
            buildList { while (rs.next()) add(rs.getString("name")) }
        }
    }

    override fun exec(sql: String) { conn.createStatement().use { it.execute(sql) } }
    override fun begin() { conn.autoCommit = false }
    override fun commit() { conn.commit(); conn.autoCommit = true }
    override fun rollback() { conn.rollback(); conn.autoCommit = true }
}
