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
            val validationError = validateOrder(itemName, price, count)

            if (validationError != null) {
                return OrderCreateResult.Failure(validationError)
            }

            val order = Order(id, itemName, price, count, status)

            return OrderCreateResult.Success(order)
        }
    }
}

sealed interface OrderDraftResult {
    data class Success(val draft: OrderDraft) : OrderDraftResult
    data class Failure(val error: OrderError) : OrderDraftResult
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

        return when (
            val result = Order.create(
                id, itemName, price, count, status
            )
        ) {
            is OrderCreateResult.Success -> result.order
            is OrderCreateResult.Failure -> {
                error("invalid order draft: ${result.error}")
            }
        }
    }

    companion object {
        fun create(
            itemName: String,
            price: Int,
            count: Int
        ): OrderDraftResult {
            val validationError = validateOrder(itemName, price, count)

            if (validationError != null) {
                return OrderDraftResult.Failure(validationError)
            }

            val orderDraft = OrderDraft(itemName, price, count)

            return OrderDraftResult.Success(orderDraft)
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
