package com.example.orderapi.presentation

import com.example.orderapi.application.OrderRepository
import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderStatus
import org.jooq.DSLContext

class FakeOrderRepository : OrderRepository {
    private val orders = mutableListOf(
        createInitialOrder(1, "Notebook", 180, 3, OrderStatus.CREATED),
        createInitialOrder(2, "Bag", 2400, 1, OrderStatus.SHIPPED)
    )

    override fun findAll(ctx: DSLContext): List<Order> {
        return orders.toList()
    }

    override fun findById(ctx: DSLContext, id: Int): Order? {
        return orders.find { it.id == id }
    }

    override fun findByIdForUpdate(ctx: DSLContext, id: Int): Order? {
        return findById(ctx, id)
    }

    override fun save(ctx: DSLContext, draft: OrderDraft): Order {
        val nextId = orders
            .maxOfOrNull { order -> order.id }
            ?.plus(1)
            ?: 1

        val order = draft.toOrder(nextId)

        orders.add(order)

        return order
    }

    override fun update(ctx: DSLContext, order: Order): Order? {
        val index = orders.indexOfFirst { it.id == order.id }

        if (index == -1) {
            return null
        }

        orders[index] = order
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
