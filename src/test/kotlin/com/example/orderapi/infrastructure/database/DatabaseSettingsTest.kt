package com.example.orderapi.infrastructure.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DatabaseSettingsTest {
    @Test
    fun `loads database settings from environment`() {
        val settings = DatabaseSettings.fromEnvironment(
            mapOf(
                "DB_URL" to "jdbc:postgresql://db:5432/order_api",
                "DB_USER" to "order_api",
                "DB_PASSWORD" to "secret",
                "DB_MAX_POOL_SIZE" to "5"
            )
        )

        assertEquals(
            "jdbc:postgresql://db:5432/order_api",
            settings.jdbcUrl
        )
        assertEquals("order_api", settings.username)
        assertEquals("secret", settings.password)
        assertEquals(5, settings.maximumPoolSize)
    }

    @Test
    fun `uses default pool size when it is not specified`() {
        val settings = DatabaseSettings.fromEnvironment(
            mapOf(
                "DB_URL" to "jdbc:postgresql://db:5432/order_api",
                "DB_USER" to "order_api",
                "DB_PASSWORD" to "secret"
            )
        )

        assertEquals(10, settings.maximumPoolSize)
    }

    @Test
    fun `fails when required setting is missing`() {
        val exception = assertFailsWith<IllegalStateException> {
            DatabaseSettings.fromEnvironment(emptyMap())
        }

        assertEquals("DB_URL must be set", exception.message)
    }
}
