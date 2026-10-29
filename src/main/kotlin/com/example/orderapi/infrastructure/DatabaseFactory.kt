package com.example.orderapi.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

class DatabaseFactory(settings: DatabaseSettings) : AutoCloseable {
    private val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = settings.jdbcUrl
            username = settings.username
            password = settings.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = settings.maximumPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            poolName = "order-api-pool"
            validate()
        }
    )

    private val dsl: DSLContext = DSL.using(dataSource, SQLDialect.POSTGRES)

    val database: Database = Database(dsl)

    override fun close() {
        dataSource.close()
    }
}
