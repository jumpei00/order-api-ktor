package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderDraftResult
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.Database

class OrderService(
    private val database: Database,
    private val orderRepository: OrderRepository,
    private val orderStatusHistoryRepository: OrderStatusHistoryRepository
) {
    fun listOrders(): List<Order> {
        return database.transaction { ctx -> orderRepository.findAll(ctx) }
    }

    fun findOrder(id: Int): Order? {
        return database.transaction { ctx -> orderRepository.findById(ctx, id) }
    }

    fun createOrder(
        itemName: String,
        price: Int,
        count: Int
    ): OrderCreateResult {
        val draftResult = OrderDraft.create(
            itemName = itemName,
            price = price,
            count = count,
        )

        return when (draftResult) {
            is OrderDraftResult.Success -> {
                val savedOrder = database.transaction { ctx -> orderRepository.save(ctx, draftResult.draft) }
                OrderCreateResult.Success(savedOrder)
            }

            is OrderDraftResult.Failure -> {
                OrderCreateResult.Failure(draftResult.error)
            }
        }
    }

    fun updateStatus(id: Int, status: OrderStatus): OrderUpdateStatusResult {
        return database.transaction { ctx ->
            val order = orderRepository.findByIdForUpdate(ctx, id)
                ?: return@transaction OrderUpdateStatusResult.NotFound
            val savedOrder = orderRepository.update(ctx, order.changeStatus(status))
                ?: return@transaction OrderUpdateStatusResult.NotFound

            orderStatusHistoryRepository.append(ctx, savedOrder.id, savedOrder.status)

            OrderUpdateStatusResult.Success(savedOrder)
        }
    }
}
