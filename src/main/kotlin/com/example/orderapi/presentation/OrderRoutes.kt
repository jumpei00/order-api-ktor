package com.example.orderapi.presentation

import com.example.orderapi.application.OrderService
import com.example.orderapi.application.OrderUpdateStatusError
import com.example.orderapi.domain.OrderStatus
import com.github.michaelbull.result.mapBoth
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.patch


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

        orderService.createOrder(
            itemName = request.itemName,
            price = request.price,
            count = request.count
        ).mapBoth(
            { order ->
                call.respond(
                    status = HttpStatusCode.Created,
                    message = order.toResponse()
                )
            },
            { error ->
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(error.message())
                )

            }
        )
    }

    patch("/orders/{id}/status") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse("id must be an integer")
            )
            return@patch
        }

        val request = call.receive<UpdateOrderStatusRequest>()

        val status = OrderStatus.entries.find { it.name == request.status }

        if (status == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse("status is invalid")
            )
            return@patch
        }

        orderService.updateStatus(id, status).mapBoth(
            { order ->
                call.respond(
                    status = HttpStatusCode.OK,
                    message = order.toResponse()
                )
            },
            { error ->
                when (error) {
                    OrderUpdateStatusError.NotFound -> call.respond(
                        status = HttpStatusCode.NotFound,
                        message = ErrorResponse("order not found")
                    )
                }
            }
        )
    }
}
