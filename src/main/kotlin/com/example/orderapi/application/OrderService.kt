package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderDraftResult
import com.example.orderapi.domain.OrderStatus

class OrderService(
    private val orderRepository: OrderRepository
) {
    fun listOrders(): List<Order> {
        return orderRepository.findAll()
    }

    fun findOrder(id: Int): Order? {
        return orderRepository.findById(id)
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
                val savedOrder = orderRepository.save(draftResult.draft)
                OrderCreateResult.Success(savedOrder)
            }

            is OrderDraftResult.Failure -> {
                OrderCreateResult.Failure(draftResult.error)
            }
        }
    }

    fun updateStatus(id: Int, status: OrderStatus): OrderUpdateStatusResult {
        val order = orderRepository.findById(id) ?: return OrderUpdateStatusResult.NotFound
        val updateOrder = order.changeStatus(status)
        val savedOrder = orderRepository.update(updateOrder) ?: return OrderUpdateStatusResult.NotFound
        return OrderUpdateStatusResult.Success(savedOrder)
    }
}

