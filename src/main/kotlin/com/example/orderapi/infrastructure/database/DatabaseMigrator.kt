package com.example.orderapi.infrastructure.database

import org.flywaydb.core.Flyway

object DatabaseMigrator {
    fun migrate(settings: DatabaseSettings) {
        val result = Flyway.configure()
            .dataSource(
                settings.jdbcUrl,
                settings.username,
                settings.password
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()

        println("Database migration completed: " + "${result.migrationsExecuted} migration(s) executed")
    }
}
