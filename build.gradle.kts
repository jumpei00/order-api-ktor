import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("io.ktor.plugin") version "3.5.0"
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
    application
}

group = "com.example.orderapi"
version = "0.1.0"

application {
    mainClass.set("com.example.orderapi.ApplicationKt")
}

ktor {
  fatJar {
    archiveFileName.set("app.jar")
  }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("ch.qos.logback:logback-classic:1.5.20")

    implementation("org.postgresql:postgresql:42.7.13")
    implementation("com.zaxxer:HikariCP:7.0.2")

    implementation("org.jooq:jooq:3.21.9")
    implementation("org.jooq:jooq-kotlin:3.21.9")
    jooqCodegen("org.postgresql:postgresql:42.7.13")

    implementation("org.flywaydb:flyway-core:12.9.0")
    implementation("org.flywaydb:flyway-database-postgresql:12.9.0")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(kotlin("test-junit5"))
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = System.getenv("JOOQ_DB_URL") ?: "jdbc:postgresql://localhost:5432/order_api"
            user = System.getenv("DB_USER")
            password = System.getenv("DB_PASSWORD")
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }

            target {
                packageName = "com.example.orderapi.infrastructure.database.jooq"
                directory = "src/main/generated"
            }
        }
    }
}

sourceSets.main {
    kotlin.srcDir("src/main/generated")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    mergeServiceFiles()

    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
