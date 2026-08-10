package com.notes.data.messaging

import com.notes.application.messaging.CreateNoteMessage
import com.notes.application.messaging.NoteCommandPublisher
import com.notes.application.messaging.UpdateNoteMessage
import com.rabbitmq.client.AMQP
import kotlinx.serialization.json.Json

class RabbitMqNoteCommandPublisher(
    private val connection: RabbitMqConnection
) : NoteCommandPublisher {

    override fun publishCreate(message: CreateNoteMessage) {
        connection.createChannel().use { channel ->

            channel.queueDeclare(
                CREATE_NOTE_QUEUE,
                true,
                false,
                false,
                null
            )

            channel.confirmSelect()

            val payload = Json
                .encodeToString(message)
                .encodeToByteArray()

            channel.basicPublish(
                "",
                CREATE_NOTE_QUEUE,
                AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .deliveryMode(2)
                    .messageId(message.commandId)
                    .build(),
                payload
            )

            channel.waitForConfirmsOrDie()
        }
    }

    override fun publishUpdate(
        message: UpdateNoteMessage
    ) {
        connection.createChannel().use { channel ->
            RabbitMqTopology.declare(channel)
            channel.confirmSelect()
            val payload = Json
                .encodeToString(message)
                .encodeToByteArray()
            channel.basicPublish(
                RabbitMqTopology.COMMAND_EXCHANGE,
                RabbitMqTopology.UPDATE_ROUTING_KEY,
                AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .deliveryMode(2)
                    .messageId(message.commandId)
                    .build(),
                payload
            )
            channel.waitForConfirmsOrDie()
        }
    }

    companion object {
        const val CREATE_NOTE_QUEUE = "notes.create"
        const val UPDATE_NOTE_QUEUE = "notes.update"
    }
}