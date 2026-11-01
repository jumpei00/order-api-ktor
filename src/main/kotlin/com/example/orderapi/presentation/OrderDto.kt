package com.example.orderapi.presentation

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderError

data class OrderResponse(
    val id: Int,
    val itemName: String,
    val total: Int,
    val status: String
)

data class CreateOrderRequest(
    val itemName: String,
    val price: Int,
    val count: Int
)

data class ErrorResponse(
    val message: String
)

fun Order.toResponse(): OrderResponse {
    return OrderResponse(id, itemName, total, status.name)
}

fun OrderError.message(): String {
    return when (this) {
        OrderError.BlankItemName -> "itemName must not be blank"
        OrderError.NegativePrice -> "price must be non-negative"
        OrderError.InvalidCount -> "count must be positive"
    }
}

data class UpdateOrderStatusRequest(
    val status: String
)
