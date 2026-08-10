package com.notes.plugins
import com.notes.data.database.DatabaseFactory
import io.ktor.server.application.Application

fun Application.configureDatabase() {

    val url = environment.config.property("database.url").getString()

    val user = environment.config.property("database.user").getString()

    val password = environment.config.property("database.password").getString()

    DatabaseFactory.init(
        jdbcUrl = url,
        username = user,
        password = password
    )

}