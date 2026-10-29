package com.example.orderapi

import com.example.orderapi.infrastructure.Database
import io.mockk.every
import io.mockk.mockk
import org.jooq.DSLContext

fun fakeDatabase(): Database {
    val ctx = mockk<DSLContext>()

    return mockk {
        every { transaction<Any?>(any()) } answers {
            firstArg<(DSLContext) -> Any?>().invoke(ctx)
        }
    }
}
