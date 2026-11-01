package com.example.orderapi.infrastructure

import com.github.michaelbull.result.Result
import org.jooq.DSLContext

class Database(private val dsl: DSLContext) {
    fun <T> transaction(block: (DSLContext) -> T): T {
        return dsl.transactionResult { config -> block(config.dsl()) }
    }
}

private class RollbackSignal(val result: Result<*, *>) : RuntimeException(null, null, false, false)

fun <V, E> Database.transactionResult(block: (DSLContext) -> Result<V, E>): Result<V, E> {
    return try {
        transaction { ctx ->
            val result = block(ctx)

            if (result.isErr) {
                throw RollbackSignal(result)
            }

            result
        }
    } catch (signal: RollbackSignal) {
        @Suppress("UNCHECKED_CAST")
        signal.result as Result<V, E>
    }
}

