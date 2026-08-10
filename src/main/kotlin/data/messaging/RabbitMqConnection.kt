package com.notes.data.messaging

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class RabbitMqConnection(
    host: String,
    port: Int,
    username: String,
    password: String
) {

    private val connection: Connection

    init {
        val factory = ConnectionFactory().apply {
            this.host = host
            this.port = port
            this.username = username
            this.password = password

            isAutomaticRecoveryEnabled = true
            networkRecoveryInterval = 5_000
        }

        connection = factory.newConnection(
            "notes-api"
        )
    }

    fun createChannel() =
        connection.createChannel()

    fun close() {
        connection.close()
    }
}