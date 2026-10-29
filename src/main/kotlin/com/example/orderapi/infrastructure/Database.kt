package com.example.orderapi.infrastructure

import org.jooq.DSLContext

class Database(private val dsl: DSLContext) {
    fun <T> transaction(block: (DSLContext) -> T): T {
        return dsl.transactionResult { config -> block(config.dsl()) }
    }
}

