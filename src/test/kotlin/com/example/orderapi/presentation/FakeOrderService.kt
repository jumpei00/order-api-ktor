package com.example.orderapi.presentation

import com.example.orderapi.application.OrderService
import io.mockk.mockk

fun fakeOrderService(): OrderService {
    return OrderService(fakeDatabase(), FakeOrderRepository(), mockk(relaxed = true))
}
