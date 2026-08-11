package com.example.orderapi.infrastructure.database

data class DatabaseSettings(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int
) {
    companion object {
        fun fromEnvironment(
            environment: Map<String, String> = System.getenv()
        ): DatabaseSettings {
            return DatabaseSettings(
                jdbcUrl = environment.required("DB_URL"),
                username = environment.required("DB_USER"),
                password = environment.required("DB_PASSWORD"),
                maximumPoolSize = environment["DB_MAX_POOL_SIZE"]?.toIntOrNull()?.takeIf { it > 0 } ?: 10
            )
        }
    }
}

private fun Map<String, String>.required(name: String): String {
    return this[name]?.takeIf { it.isNotBlank() } ?: error("$name must be set")
}
