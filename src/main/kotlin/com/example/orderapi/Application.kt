package com.example.orderapi

import com.example.orderapi.application.OrderService
import com.example.orderapi.application.OrderRepository
import com.example.orderapi.application.OrderStatusHistoryRepository
import com.example.orderapi.infrastructure.Database
import com.example.orderapi.infrastructure.DatabaseFactory
import com.example.orderapi.infrastructure.DatabaseMigrator
import com.example.orderapi.infrastructure.DatabaseSettings
import com.example.orderapi.infrastructure.OrderRepositoryImp
import com.example.orderapi.infrastructure.OrderStatusHistoryRepositoryImp
import com.example.orderapi.presentation.orderRoutes
import com.google.inject.Guice
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import com.fasterxml.jackson.databind.cfg.CoercionAction
import com.fasterxml.jackson.databind.cfg.CoercionInputShape
import com.fasterxml.jackson.databind.type.LogicalType

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

fun Application.module(orderService: OrderService) {
    install(ContentNegotiation) {
        jackson {
            coercionConfigFor(LogicalType.Integer)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
        }
    }

    routing {
        orderRoutes(orderService)
    }
}

private fun startServer() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val settings = DatabaseSettings.fromEnvironment()
    val injector = Guice.createInjector(AppModule(settings))

    injector.getInstance(DatabaseFactory::class.java).use {
        val orderService = injector.getInstance(OrderService::class.java)

        embeddedServer(Netty, port = port) {
            module(orderService)
        }.start(wait = true)
    }
}
