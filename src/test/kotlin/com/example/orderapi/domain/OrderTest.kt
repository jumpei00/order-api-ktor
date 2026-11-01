package com.example.orderapi.domain

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class OrderTest {
    @Test
    fun `create returns ok when values are valid`() {
        val order = Order.create(
            id = 1,
            itemName = "Notebook",
            price = 180,
            count = 3,
            status = OrderStatus.CREATED
        ).okOrFail()

        assertEquals(1, order.id)
        assertEquals("Notebook", order.itemName)
        assertEquals(540, order.total)
        assertEquals(OrderStatus.CREATED, order.status)
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

        assertEquals(Err(OrderError.BlankItemName), result)
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

        assertEquals(Err(OrderError.NegativePrice), result)
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

        assertEquals(Err(OrderError.InvalidCount), result)
    }

    @Test
    fun `changeStatus returns order with new status`() {
        val order = Order.create(
            id = 1,
            itemName = "Notebook",
            price = 180,
            count = 3,
            status = OrderStatus.CREATED
        ).okOrFail()

        val paidOrder = order.changeStatus(OrderStatus.PAID)

        assertEquals(OrderStatus.CREATED, order.status)
        assertEquals(OrderStatus.PAID, paidOrder.status)
        assertEquals(order.id, paidOrder.id)
        assertEquals(order.total, paidOrder.total)
    }

    @Test
    fun `order draft is created without persisted id`() {
        val draft = OrderDraft.create(
            itemName = "Notebook",
            price = 180,
            count = 3
        ).okOrFail()

        assertEquals("Notebook", draft.itemName)
        assertEquals(180, draft.price)
        assertEquals(3, draft.count)
        assertEquals(OrderStatus.CREATED, draft.status)
    }

    @Test
    fun `order draft rejects blank item name`() {
        val result = OrderDraft.create(
            itemName = "",
            price = 180,
            count = 3
        )

        assertEquals(Err(OrderError.BlankItemName), result)
    }

    private fun <V> Result<V, OrderError>.okOrFail(): V {
        return getOrElse { reason -> fail("expected Ok but was Err($reason)") }
    }
}
