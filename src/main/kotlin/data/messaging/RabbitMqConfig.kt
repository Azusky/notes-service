package com.notes.data.messaging

data class RabbitMqConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String
)