package com.example.orderapi.application

import com.example.orderapi.domain.OrderStatus
import org.jooq.DSLContext

interface OrderStatusHistoryRepository {
    fun append(ctx: DSLContext, orderId: Int, status: OrderStatus)
}
