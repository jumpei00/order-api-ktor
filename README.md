# order-api-ktor

A small order API built with Kotlin and Ktor.

## Requirements

- JDK 21

## Run

```sh
./gradlew run
```

The server starts on port `8080` by default. Set `PORT` to override it.

## Test

```sh
./gradlew test
```

## Endpoints

- `GET /` - health check
- `GET /orders` - list orders
- `GET /orders/{id}` - get an order
- `POST /orders` - create an order
