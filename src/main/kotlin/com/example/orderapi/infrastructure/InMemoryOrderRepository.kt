package com.example.orderapi.infrastructure

import com.example.orderapi.application.OrderRepository
import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderStatus

class InMemoryOrderRepository : OrderRepository {
    private val orders = mutableListOf(
        createInitialOrder(1, "Notebook", 180, 3, OrderStatus.CREATED),
        createInitialOrder(2, "Bag", 2400, 1, OrderStatus.SHIPPED)
    )

    override fun findAll(): List<Order> {
        return orders.toList()
    }

    override fun findById(id: Int): Order? {
        return orders.find { it.id == id }
    }

    override fun save(order: Order): Order {
        orders.add(order)
        return order
    }

    private fun createInitialOrder(
        id: Int,
        itemName: String,
        price: Int,
        count: Int,
        status: OrderStatus
    ): Order {
        return when (val result = Order.create(id, itemName, price, count, status)) {
            is OrderCreateResult.Success -> result.order
            is OrderCreateResult.Failure -> error("invalid initial order: ${result.error}")
        }
    }
}
