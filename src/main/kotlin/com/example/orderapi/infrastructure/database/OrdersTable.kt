package com.example.orderapi.infrastructure.database

import com.example.orderapi.domain.OrderStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object OrdersTable : Table("orders") {
    val id = integer("id").autoIncrement()
    val itemName = varchar("item_name", 200)
    val price = integer("price")
    val count = integer("count")
    val status = enumerationByName("status", 20, OrderStatus::class)

    val createdAt = timestampWithTimeZone("created_at").databaseGenerated()
    val updatedAt = timestampWithTimeZone("updated_at").databaseGenerated()

    override val primaryKey = PrimaryKey(id)
}
