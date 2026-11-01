package com.example.orderapi

import com.example.orderapi.application.OrderRepository
import com.example.orderapi.application.OrderService
import com.example.orderapi.application.OrderStatusHistoryRepository
import com.example.orderapi.infrastructure.Database
import com.example.orderapi.infrastructure.DatabaseFactory
import com.example.orderapi.infrastructure.DatabaseSettings
import com.google.inject.Guice
import com.google.inject.Injector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertNotNull
import kotlin.test.assertSame

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppModuleTest {
    private val postgres = PostgreSQLContainer("postgres:18.4-alpine")
    private lateinit var injector: Injector

    @BeforeAll
    fun setUp() {
        postgres.start()

        val settings = DatabaseSettings(
            jdbcUrl = postgres.jdbcUrl,
            username = postgres.username,
            password = postgres.password,
            maximumPoolSize = 2
        )

        injector = Guice.createInjector(AppModule(settings))
    }

    @AfterAll
    fun tearDown() {
        injector.getInstance(DatabaseFactory::class.java).close()
        postgres.stop()
    }

    @Test
    fun `OrderService is resolved with all its dependencies`() {
        assertNotNull(injector.getInstance(OrderService::class.java))
    }

    @Test
    fun `singleton bindings return the same instance`() {
        assertSame(
            injector.getInstance(DatabaseFactory::class.java),
            injector.getInstance(DatabaseFactory::class.java)
        )
        assertSame(
            injector.getInstance(Database::class.java),
            injector.getInstance(Database::class.java)
        )
        assertSame(
            injector.getInstance(OrderRepository::class.java),
            injector.getInstance(OrderRepository::class.java)
        )
        assertSame(
            injector.getInstance(OrderStatusHistoryRepository::class.java),
            injector.getInstance(OrderStatusHistoryRepository::class.java)
        )
        assertSame(
            injector.getInstance(OrderService::class.java),
            injector.getInstance(OrderService::class.java)
        )
    }

    @Test
    fun `Database is provided by the single DatabaseFactory`() {
        assertSame(
            injector.getInstance(DatabaseFactory::class.java).database,
            injector.getInstance(Database::class.java)
        )
    }
}
