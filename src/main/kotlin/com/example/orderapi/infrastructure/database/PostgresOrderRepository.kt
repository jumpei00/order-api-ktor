package com.example.orderapi.infrastructure.database

import com.example.orderapi.application.OrderRepository
import com.example.orderapi.domain.Order
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class PostgresOrderRepository(private val database: Database) : OrderRepository {
    override fun findAll(): List<Order> {
        return transaction(db = database) {
            OrdersTable
                .selectAll()
                .orderBy(OrdersTable.id to SortOrder.ASC)
                .map { row -> row.toOrder() }
        }
    }

    override fun findById(id: Int): Order? {
        return transaction(db = database) {
            OrdersTable
                .selectAll()
                .where { OrdersTable.id eq id }
                .firstOrNull()
                ?.toOrder()
        }
    }

    override fun save(order: Order): Order {
        return transaction(db = database) {
            OrdersTable.insert { statements ->
                statements[OrdersTable.id] = order.id
                statements[OrdersTable.itemName] = order.itemName
                statements[OrdersTable.price] = order.price
                statements[OrdersTable.count] = order.count
                statements[OrdersTable.status] = order.status
            }
            order
        }
    }

    override fun update(order: Order): Order? {
        return transaction(db = database) {
            val updatedRows = OrdersTable.update(
                where = { OrdersTable.id eq order.id }
            ) { statements ->
                statements[OrdersTable.itemName] = order.itemName
                statements[OrdersTable.price] = order.price
                statements[OrdersTable.count] = order.count
                statements[OrdersTable.status] = order.status
            }

            if (updatedRows == 0) {
                null
            } else {
                order
            }
        }
    }
}
