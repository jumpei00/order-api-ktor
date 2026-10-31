package com.example.orderapi.application

import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.DatabaseFactory
import com.example.orderapi.infrastructure.DatabaseMigrator
import com.example.orderapi.infrastructure.DatabaseSettings
import com.example.orderapi.infrastructure.OrderRepositoryImp
import com.example.orderapi.infrastructure.OrderStatusHistoryRepositoryImp
import com.example.orderapi.infrastructure.jooq.tables.references.ORDERS
import com.example.orderapi.infrastructure.jooq.tables.references.ORDER_STATUS_HISTORIES
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderServiceTransactionTest {
    private val postgres = PostgreSQLContainer("postgres:18.4-alpine")
    private lateinit var databaseFactory: DatabaseFactory

    @BeforeAll
    fun setUpDatabase() {
        postgres.start()

        val settings = DatabaseSettings(
            jdbcUrl = postgres.jdbcUrl,
            username = postgres.username,
            password = postgres.password,
            maximumPoolSize = 2
        )

        DatabaseMigrator.migrate(settings)
        databaseFactory = DatabaseFactory(settings)
    }

    @AfterAll
    fun tearDownDatabase() {
        databaseFactory.close()
        postgres.stop()
    }

    @BeforeEach
    fun cleanTables() {
        databaseFactory.database.transaction { ctx ->
            ctx.deleteFrom(ORDER_STATUS_HISTORIES).execute()
            ctx.deleteFrom(ORDERS).execute()
        }
    }

    @Test
    fun `updateStatus commits the order update and its history together`() {
        val service = OrderService(
            databaseFactory.database,
            OrderRepositoryImp(),
            OrderStatusHistoryRepositoryImp()
        )
        val orderId = createOrder(service)

        service.updateStatus(orderId, OrderStatus.PAID)

        assertEquals(OrderStatus.PAID, service.findOrder(orderId)?.status)
        assertEquals(1, countHistories())
    }

    @Test
    fun `updateStatus rolls back the order update when the history insert fails`() {
        val failingHistoryRepository = mockk<OrderStatusHistoryRepository> {
            every { append(any(), any(), any()) } throws IllegalStateException("history failure")
        }
        val service = OrderService(
            databaseFactory.database,
            OrderRepositoryImp(),
            failingHistoryRepository
        )
        val orderId = createOrder(service)

        assertFailsWith<IllegalStateException> {
            service.updateStatus(orderId, OrderStatus.PAID)
        }

        assertEquals(OrderStatus.CREATED, service.findOrder(orderId)?.status)
        assertEquals(0, countHistories())
    }

    private fun createOrder(service: OrderService): Int {
        val result = service.createOrder(itemName = "pen", price = 100, count = 2)
        return (result as OrderCreateResult.Success).order.id
    }

    private fun countHistories(): Int {
        return databaseFactory.database.transaction { ctx ->
            ctx.fetchCount(ORDER_STATUS_HISTORIES)
        }
    }
}
