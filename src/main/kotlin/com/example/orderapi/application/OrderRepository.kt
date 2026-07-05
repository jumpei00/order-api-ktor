package com.example.orderapi.application

import com.example.orderapi.domain.Order

interface OrderRepository {
    fun findAll(): List<Order>
    fun findById(id: Int): Order?
    fun save(order: Order): Order
}
