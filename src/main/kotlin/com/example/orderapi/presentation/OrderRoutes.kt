package com.example.orderapi.presentation

import com.example.orderapi.application.OrderService
import com.example.orderapi.domain.OrderCreateResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.orderRoutes(orderService: OrderService) {
    get("/orders") {
        val orders = orderService.listOrders()
        call.respond(orders.map { it.toResponse() })
    }

    get("/orders/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse("id must be an integer")
            )
            return@get
        }

        val order = orderService.findOrder(id)

        if (order == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse("order not found")
            )
            return@get
        }

        call.respond(order.toResponse())
    }

    post("/orders") {
        val request = call.receive<CreateOrderRequest>()

        when (val result = orderService.createOrder(
            itemName = request.itemName,
            price = request.price,
            count = request.count
        )) {
            is OrderCreateResult.Success -> {
                call.respond(
                    status = HttpStatusCode.Created,
                    message = result.order.toResponse()
                )
            }

            is OrderCreateResult.Failure -> {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(result.error.message())
                )
            }
        }
    }
}
