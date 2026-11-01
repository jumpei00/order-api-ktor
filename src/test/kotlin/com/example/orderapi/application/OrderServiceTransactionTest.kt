package com.example.orderapi.application

import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.DatabaseFactory
import com.example.orderapi.infrastructure.DatabaseMigrator
import com.example.orderapi.infrastructure.DatabaseSettings
import com.example.orderapi.infrastructure.OrderRepositoryImp
import com.example.orderapi.infrastructure.OrderStatusHistoryRepositoryImp
import com.example.orderapi.infrastructure.jooq.tables.references.ORDERS
import com.example.orderapi.infrastructure.jooq.tables.references.ORDER_STATUS_HISTORIES
import com.example.orderapi.infrastructure.transactionResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapBoth
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
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

    @Test
    fun `transactionResult rolls back when the block returns Err`() {
        val draft = OrderDraft.create(itemName = "pen", price = 100, count = 2)
            .getOrElse { reason -> fail("expected Ok but was Err($reason)") }

        val result = databaseFactory.database.transactionResult { ctx ->
            OrderRepositoryImp().save(ctx, draft)
            Err("business failure")
        }

        val error = result.mapBoth(
            { value -> fail("expected Err but was Ok($value)") },
            { reason -> reason }
        )

        assertEquals("business failure", error)
        assertEquals(0, countOrders())
    }

    @Test
    fun `transactionResult commits when the block returns Ok`() {
        val draft = OrderDraft.create(itemName = "pen", price = 100, count = 2)
            .getOrElse { reason -> fail("expected Ok but was Err($reason)") }

        databaseFactory.database.transactionResult { ctx ->
            Ok(OrderRepositoryImp().save(ctx, draft))
        }

        assertEquals(1, countOrders())
    }

    private fun createOrder(service: OrderService): Int {
        return service.createOrder(itemName = "pen", price = 100, count = 2)
            .getOrElse { reason -> fail("expected Ok but wa Err($reason)") }
            .id

    }

    private fun countHistories(): Int {
        return databaseFactory.database.transaction { ctx ->
            ctx.fetchCount(ORDER_STATUS_HISTORIES)
        }
    }

    private fun countOrders(): Int {
        return databaseFactory.database.transaction { ctx ->
            ctx.fetchCount(ORDERS)
        }
    }
}
