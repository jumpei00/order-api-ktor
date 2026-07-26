package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
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
        val nextId = orderRepository.findAll().maxOfOrNull { it.id }?.plus(1) ?: 1
        val result = Order.create(
            id = nextId,
            itemName = itemName,
            price = price,
            count = count,
            status = OrderStatus.CREATED
        )

        return when (result) {
            is OrderCreateResult.Success -> {
                val savedOrder = orderRepository.save(result.order)
                OrderCreateResult.Success(savedOrder)
            }

            is OrderCreateResult.Failure -> result
        }
    }

    fun updateStatus(id: Int, status: OrderStatus): OrderUpdateStatusResult {
        val order = orderRepository.findById(id) ?: return OrderUpdateStatusResult.NotFound
        val updateOrder = order.changeStatus(status)
        val savedOrder = orderRepository.update(updateOrder) ?: return OrderUpdateStatusResult.NotFound
        return OrderUpdateStatusResult.Success(savedOrder)
    }
}

