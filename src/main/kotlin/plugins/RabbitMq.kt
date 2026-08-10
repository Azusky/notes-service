package com.notes.plugins


import com.notes.data.messaging.RabbitMqConnection
import io.ktor.server.application.Application

fun Application.rabbitMqConnection(): RabbitMqConnection {
    val config = environment.config

    return RabbitMqConnection(
        host = config.property("rabbitmq.host").getString(),
        port = config.property("rabbitmq.port").getString().toInt(),
        username = config.property("rabbitmq.username").getString(),
        password = config.property("rabbitmq.password").getString()
    )
}