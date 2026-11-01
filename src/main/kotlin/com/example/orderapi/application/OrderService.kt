package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderError
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.Database
import com.example.orderapi.infrastructure.transactionResult
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.map
import com.github.michaelbull.result.toResultOr

sealed interface OrderUpdateStatusError {
    data object NotFound : OrderUpdateStatusError
}

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
    ): Result<Order, OrderError> {
        return OrderDraft.create(
            itemName = itemName,
            price = price,
            count = count,
        ).map { draft -> database.transaction { ctx -> orderRepository.save(ctx, draft) } }
    }

    fun updateStatus(id: Int, status: OrderStatus): Result<Order, OrderUpdateStatusError> {
        return database.transactionResult { ctx ->
            binding {
                val order = orderRepository.findByIdForUpdate(ctx, id)
                    .toResultOr { OrderUpdateStatusError.NotFound }
                    .bind()

                val savedOrder = orderRepository.update(ctx, order.changeStatus(status))
                    .toResultOr { OrderUpdateStatusError.NotFound }
                    .bind()

                orderStatusHistoryRepository.append(ctx, savedOrder.id, savedOrder.status)

                savedOrder
            }
        }
    }
}
