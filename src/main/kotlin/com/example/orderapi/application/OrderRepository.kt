package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderDraft

interface OrderRepository {
    fun findAll(): List<Order>
    fun findById(id: Int): Order?
    fun save(draft: OrderDraft): Order
    fun update(order: Order): Order?
}
