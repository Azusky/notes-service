package com.notes.plugins

import com.notes.di.appModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.notes.data.messaging.RabbitMqConfig

fun Application.configureDependencyInjection() {
    val rabbitMqConfig = RabbitMqConfig(
        host = environment.config
            .property("rabbitmq.host")
            .getString(),
        port = environment.config
            .property("rabbitmq.port")
            .getString()
            .toInt(),
        username = environment.config
            .property("rabbitmq.username")
            .getString(),
        password = environment.config
            .property("rabbitmq.password")
            .getString()
    )
    install(Koin) {
        slf4jLogger()
        modules(
            appModule(
                rabbitMqConfig = rabbitMqConfig
            )
        )
    }
}
