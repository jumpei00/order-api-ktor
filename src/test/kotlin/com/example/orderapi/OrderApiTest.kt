package com.example.orderapi

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderApiTest {
    @Test
    fun `Get orders returns order list`() = testApplication {
        application {
            module()
        }

        val response = client.get("/orders")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("Notebook"))
        assertTrue(body.contains("Bag"))
    }

    @Test
    fun `GET order by id returns one order`() = testApplication {
        application {
            module()
        }

        val response = client.get("/orders/1")
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("\"id\":1"))
        assertTrue(body.contains("Notebook"))
    }

    @Test
    fun `POST orders returns bad request when item name is blank`() = testApplication {
        application {
            module()
        }

        val response = client.post("/orders") {
            contentType(ContentType.Application.Json)
            setBody("""{"itemName":"","price":100,"count":5}""")
        }
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(body.contains("itemName must not be blank"))
    }

    @Test
    fun `POST orders creates order`() = testApplication {
        application {
            module()
        }

        val response = client.post("/orders") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"itemName":"Pencil","price":100,"count":5}"""
            )
        }
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(body.contains("\"id\":3"))
        assertTrue(body.contains("Pencil"))
        assertTrue(body.contains("\"total\":500"))
        assertTrue(body.contains("CREATED"))
    }

    @Test
    fun `PATCH order status updates existing order`() = testApplication {
        application {
            module()
        }

        val response = client.patch("/orders/1/status") {
            contentType(ContentType.Application.Json)
            setBody("""{"status":"PAID"}""")
        }
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(body.contains("\"id\":1"))
        assertTrue(body.contains("\"status\":\"PAID\""))

        val getResponse = client.get("/orders/1")
        val getBody = getResponse.bodyAsText()

        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertTrue(getBody.contains("\"status\":\"PAID\""))
    }

    @Test
    fun `PATCH order status returns not found`() = testApplication {
        application {
            module()
        }

        val response = client.patch("/orders/999/status") {
            contentType(ContentType.Application.Json)
            setBody("""{"status":"PAID"}""")
        }
        val body = response.bodyAsText()

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(body.contains("order not found"))
    }

    @Test
    fun `PATCH order status returns bad request for invalid status`() =
        testApplication {
            application {
                module()
            }

            val response = client.patch("/orders/1/status") {
                contentType(ContentType.Application.Json)
                setBody("""{"status":"UNKNOWN"}""")
            }
            val body = response.bodyAsText()

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(body.contains("status is invalid"))
        }
}
