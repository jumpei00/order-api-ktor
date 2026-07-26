package com.example.orderapi.domain

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

sealed interface OrderCreateResult {
    data class Success(val order: Order) : OrderCreateResult
    data class Failure(val error: OrderError) : OrderCreateResult
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
        ): OrderCreateResult {
            if (itemName.isBlank()) {
                return OrderCreateResult.Failure(OrderError.BlankItemName)
            }

            if (price < 0) {
                return OrderCreateResult.Failure(OrderError.NegativePrice)
            }

            if (count <= 0) {
                return OrderCreateResult.Failure(OrderError.InvalidCount)
            }

            val order = Order(
                id, itemName, price, count, status
            )

            return OrderCreateResult.Success(order)
        }
    }
}
