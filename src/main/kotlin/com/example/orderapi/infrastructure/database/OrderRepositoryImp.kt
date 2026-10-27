package com.example.orderapi.infrastructure.database

import com.example.orderapi.application.OrderRepository
import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import com.example.orderapi.domain.OrderDraft
import com.example.orderapi.domain.OrderStatus
import com.example.orderapi.infrastructure.database.jooq.tables.records.OrdersRecord
import com.example.orderapi.infrastructure.database.jooq.tables.references.ORDERS
import org.jooq.DSLContext

class OrderRepositoryImp(private val dsl: DSLContext) : OrderRepository {
    override fun findAll(): List<Order> {
        return dsl
            .selectFrom(ORDERS)
            .orderBy(ORDERS.ID.asc())
            .fetch()
            .map { record -> record.toOrder() }

    }

    override fun findById(id: Int): Order? {
        return dsl
            .selectFrom(ORDERS)
            .where(ORDERS.ID.eq(id))
            .fetchOne()
            ?.toOrder()
    }

    override fun save(draft: OrderDraft): Order {
        return dsl.transactionResult { config ->
            val generatedId = config.dsl()
                .insertInto(ORDERS)
                .set(ORDERS.ITEM_NAME, draft.itemName)
                .set(ORDERS.PRICE, draft.price)
                .set(ORDERS.COUNT, draft.count)
                .set(ORDERS.STATUS, draft.status.name)
                .returning(ORDERS.ID)
                .fetchSingle()
                .id!!

            draft.toOrder(generatedId)
        }
    }

    override fun update(order: Order): Order? {
        return dsl.transactionResult { config ->
            val updatedRow = config.dsl()
                .update(ORDERS)
                .set(ORDERS.ITEM_NAME, order.itemName)
                .set(ORDERS.PRICE, order.price)
                .set(ORDERS.COUNT, order.count)
                .set(ORDERS.STATUS, order.status.name)
                .where(ORDERS.ID.eq(order.id))
                .execute()

            if (updatedRow == 0) null else order
        }
    }
}

private fun OrdersRecord.toOrder(): Order {
    val result = Order.create(
        id = id!!,
        itemName = itemName!!,
        price = price!!,
        count = count!!,
        status = OrderStatus.valueOf(status!!)
    )

    return when (result) {
        is OrderCreateResult.Success -> result.order
        is OrderCreateResult.Failure -> error("invalid order row: id=$id, reason=${result.error}")
    }
}

