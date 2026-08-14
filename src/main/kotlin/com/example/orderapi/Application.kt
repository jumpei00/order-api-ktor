package com.example.orderapi

import com.example.orderapi.application.OrderService
import com.example.orderapi.infrastructure.InMemoryOrderRepository
import com.example.orderapi.infrastructure.database.DatabaseMigrator
import com.example.orderapi.infrastructure.database.DatabaseSettings
import com.example.orderapi.presentation.orderRoutes
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    val command = args.firstOrNull() ?: "server"

    when (command) {
        "server" -> startServer()
        "migrate" -> {
            val settings = DatabaseSettings.fromEnvironment()
            DatabaseMigrator.migrate(settings)
        }

        else -> error("unknown command: $command")
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    val orderRepository = InMemoryOrderRepository()
    val orderService = OrderService(orderRepository)

    routing {
        get("/") {
            call.respondText(
                text = "Order API is running",
                contentType = ContentType.Text.Plain
            )
        }

        orderRoutes(orderService)
    }
}

private fun startServer() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(Netty, port = port) {
        module()
    }.start(wait = true)
}
