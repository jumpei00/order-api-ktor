package com.example.orderapi.application

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.infrastructure.OrderRepositoryImp
import com.google.inject.ImplementedBy
import org.jooq.DSLContext

@ImplementedBy(OrderRepositoryImp::class)
interface OrderRepository {
    fun findAll(ctx: DSLContext): List<Order>
    fun findById(ctx: DSLContext, id: Int): Order?
    fun findByIdForUpdate(ctx: DSLContext, id: Int): Order?
    fun save(ctx: DSLContext, draft: OrderDraft): Order
    fun update(ctx: DSLContext, order: Order): Order?
}
