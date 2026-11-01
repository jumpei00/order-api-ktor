package com.example.orderapi.application

import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.OrderStatusHistoryRepositoryImp
import com.google.inject.ImplementedBy
import org.jooq.DSLContext

@ImplementedBy(OrderStatusHistoryRepositoryImp::class)
interface OrderStatusHistoryRepository {
    fun append(ctx: DSLContext, orderId: Int, status: OrderStatus)
}
