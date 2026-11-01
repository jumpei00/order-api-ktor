package com.example.orderapi.infrastructure

import com.example.orderapi.application.OrderStatusHistoryRepository
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.jooq.tables.references.ORDER_STATUS_HISTORIES
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jooq.DSLContext

@Singleton
class OrderStatusHistoryRepositoryImp @Inject constructor() : OrderStatusHistoryRepository {
    override fun append(ctx: DSLContext, orderId: Int, status: OrderStatus) {
        ctx
            .insertInto(ORDER_STATUS_HISTORIES)
            .set(ORDER_STATUS_HISTORIES.ORDER_ID, orderId)
            .set(ORDER_STATUS_HISTORIES.STATUS, status.name)
            .execute()
    }
}

