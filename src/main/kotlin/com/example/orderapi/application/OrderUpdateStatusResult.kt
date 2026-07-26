package com.example.orderapi.application

import com.example.orderapi.domain.Order

sealed interface OrderUpdateStatusResult {
    data class Success(val order: Order) : OrderUpdateStatusResult
    data object NotFound : OrderUpdateStatusResult
}
