package com.example.orderapi.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrderTest {
    @Test
    fun `create returns success when values are valid`() {
        val result = Order.create(
            id = 1,
            itemName = "Notebook",
            price = 180,
            count = 3,
            status = OrderStatus.CREATED
        )

        val success = assertIs<OrderCreateResult.Success>(result)

        assertEquals(1, success.order.id)
        assertEquals("Notebook", success.order.itemName)
        assertEquals(540, success.order.total)
        assertEquals(OrderStatus.CREATED, success.order.status)
    }

    @Test
    fun `create returns blank item name error when item name is blank`() {
        val result = Order.create(
            id = 1,
            itemName = "",
            price = 180,
            count = 3,
            status = OrderStatus.CREATED
        )

        val failure = assertIs<OrderCreateResult.Failure>(result)

        assertEquals(OrderError.BlankItemName, failure.error)
    }

    @Test
    fun `create returns negative price error when price is negative`() {
        val result = Order.create(
            id = 1,
            itemName = "Notebook",
            price = -1,
            count = 3,
            status = OrderStatus.CREATED
        )

        val failure = assertIs<OrderCreateResult.Failure>(result)

        assertEquals(OrderError.NegativePrice, failure.error)
    }

    @Test
    fun `create returns invalid count error when count is zero`() {
        val result = Order.create(
            id = 1,
            itemName = "Notebook",
            price = 180,
            count = 0,
            status = OrderStatus.CREATED
        )

        val failure = assertIs<OrderCreateResult.Failure>(result)

        assertEquals(OrderError.InvalidCount, failure.error)
    }
}
