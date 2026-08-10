package com.notes.data.messaging

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel

object RabbitMqTopology {

    const val COMMAND_EXCHANGE = "notes.commands"

    const val CREATE_QUEUE = "notes.create"
    const val CREATE_RETRY_QUEUE = "notes.create.retry"
    const val CREATE_DLQ = "notes.create.dlq"

    const val CREATE_ROUTING_KEY = "notes.create"
    const val CREATE_RETRY_ROUTING_KEY = "notes.create.retry"
    const val CREATE_DLQ_ROUTING_KEY = "notes.create.dlq"

    const val UPDATE_QUEUE = "notes.update"
    const val UPDATE_RETRY_QUEUE = "notes.update.retry"
    const val UPDATE_DLQ = "notes.update.dlq"

    const val UPDATE_ROUTING_KEY = "notes.update"
    const val UPDATE_RETRY_ROUTING_KEY = "notes.update.retry"
    const val UPDATE_DLQ_ROUTING_KEY = "notes.update.dlq"

    private const val RETRY_DELAY_MS = 30_000

    fun declare(channel: Channel) {

        channel.exchangeDeclare(
            COMMAND_EXCHANGE,
            BuiltinExchangeType.DIRECT,
            true
        )

        channel.queueDeclare(
            CREATE_QUEUE,
            true,
            false,
            false,
            null
        )

        channel.queueBind(
            CREATE_QUEUE,
            COMMAND_EXCHANGE,
            CREATE_ROUTING_KEY
        )

        channel.queueDeclare(
            CREATE_RETRY_QUEUE,
            true,
            false,
            false,
            mapOf(
                "x-message-ttl" to RETRY_DELAY_MS,
                "x-dead-letter-exchange" to COMMAND_EXCHANGE,
                "x-dead-letter-routing-key" to CREATE_ROUTING_KEY
            )
        )

        channel.queueBind(
            CREATE_RETRY_QUEUE,
            COMMAND_EXCHANGE,
            CREATE_RETRY_ROUTING_KEY
        )

        channel.queueDeclare(
            CREATE_DLQ,
            true,
            false,
            false,
            null
        )

        channel.queueBind(
            CREATE_DLQ,
            COMMAND_EXCHANGE,
            CREATE_DLQ_ROUTING_KEY
        )

        channel.queueDeclare(
            UPDATE_QUEUE,
            true,
            false,
            false,
            null
        )

        channel.queueBind(
            UPDATE_QUEUE,
            COMMAND_EXCHANGE,
            UPDATE_ROUTING_KEY
        )

        channel.queueDeclare(
            UPDATE_RETRY_QUEUE,
            true,
            false,
            false,
            mapOf(
                "x-message-ttl" to RETRY_DELAY_MS,
                "x-dead-letter-exchange" to COMMAND_EXCHANGE,
                "x-dead-letter-routing-key" to UPDATE_ROUTING_KEY
            )
        )

        channel.queueBind(
            UPDATE_RETRY_QUEUE,
            COMMAND_EXCHANGE,
            UPDATE_RETRY_ROUTING_KEY
        )

        channel.queueDeclare(
            UPDATE_DLQ,
            true,
            false,
            false,
            null
        )

        channel.queueBind(
            UPDATE_DLQ,
            COMMAND_EXCHANGE,
            UPDATE_DLQ_ROUTING_KEY
        )
    }
}