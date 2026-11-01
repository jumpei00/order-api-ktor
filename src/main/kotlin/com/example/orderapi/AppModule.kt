package com.example.orderapi

import com.example.orderapi.infrastructure.Database
import com.example.orderapi.infrastructure.DatabaseFactory
import com.example.orderapi.infrastructure.DatabaseSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Singleton

class AppModule(private val setting: DatabaseSettings) : AbstractModule() {
    override fun configure() {
        bind(DatabaseSettings::class.java).toInstance(setting)
    }

    @Provides
    @Singleton
    fun database(databaseFactory: DatabaseFactory): Database {
        return databaseFactory.database
    }
}

