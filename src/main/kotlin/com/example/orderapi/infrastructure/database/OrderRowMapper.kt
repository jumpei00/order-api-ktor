package com.example.orderapi.infrastructure.database

import com.example.orderapi.domain.Order
import com.example.orderapi.domain.OrderCreateResult
import org.jetbrains.exposed.v1.core.ResultRow

internal fun ResultRow.toOrder(): Order {
    val result = Order.create(
        id = this[OrdersTable.id],
        itemName = this[OrdersTable.itemName],
        price = this[OrdersTable.price],
        count = this[OrdersTable.count],
        status = this[OrdersTable.status]
    )

    return when (result) {
        is OrderCreateResult.Success -> result.order
        is OrderCreateResult.Failure -> {
            error("invalid order row: id=${this[OrdersTable.id]}, reason=${result.error}")
        }
    }
}
