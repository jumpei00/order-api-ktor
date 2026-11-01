package com.example.orderapi.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse

enum class OrderStatus {
    CREATED,
    PAID,
    SHIPPED
}

sealed interface OrderError {
    data object BlankItemName : OrderError
    data object NegativePrice : OrderError
    data object InvalidCount : OrderError
}

@ConsistentCopyVisibility
data class Order private constructor(
    val id: Int,
    val itemName: String,
    val price: Int,
    val count: Int,
    val status: OrderStatus
) {
    val total: Int
        get() = price * count

    fun changeStatus(status: OrderStatus): Order {
        return Order(
            id = id,
            itemName = itemName,
            price = price,
            count = count,
            status = status
        )
    }

    companion object {
        fun create(
            id: Int,
            itemName: String,
            price: Int,
            count: Int,
            status: OrderStatus
        ): Result<Order, OrderError> {
            val validationError = validateOrder(itemName, price, count)

            if (validationError != null) {
                return Err(validationError)
            }

            return Ok(Order(id, itemName, price, count, status))
        }
    }
}

class OrderDraft private constructor(
    val itemName: String,
    val price: Int,
    val count: Int
) {
    val status: OrderStatus
        get() = OrderStatus.CREATED

    internal fun toOrder(id: Int): Order {
        require(id > 0) {
            "id must be positive"
        }

        return Order.create(id, itemName, price, count, status)
            .getOrElse { reason -> error("invalid order draft: $reason") }
    }

    companion object {
        fun create(
            itemName: String,
            price: Int,
            count: Int
        ): Result<OrderDraft, OrderError> {
            val validationError = validateOrder(itemName, price, count)

            if (validationError != null) {
                return Err(validationError)
            }

            return Ok(OrderDraft(itemName, price, count))
        }
    }
}

private fun validateOrder(
    itemName: String,
    price: Int,
    count: Int
): OrderError? {
    return when {
        itemName.isBlank() -> OrderError.BlankItemName
        price < 0 -> OrderError.NegativePrice
        count <= 0 -> OrderError.InvalidCount
        else -> null
    }
}
