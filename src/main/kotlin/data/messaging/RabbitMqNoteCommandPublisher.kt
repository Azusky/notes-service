package com.notes.data.messaging

import com.notes.application.messaging.CreateNoteMessage
import com.notes.application.messaging.NoteCommandPublisher
import com.notes.application.messaging.UpdateNoteMessage
import com.rabbitmq.client.AMQP
import kotlinx.serialization.json.Json

class RabbitMqNoteCommandPublisher(
    private val connection: RabbitMqConnection
) : NoteCommandPublisher {

    override fun publishCreate(
        message: CreateNoteMessage
    ) {
        connection.createChannel().use { channel ->
            RabbitMqTopology.declare(channel)

            channel.confirmSelect()

            val payload = Json
                .encodeToString(message)
                .encodeToByteArray()

            channel.basicPublish(
                RabbitMqTopology.COMMAND_EXCHANGE,
                RabbitMqTopology.CREATE_ROUTING_KEY,
                persistentProperties(message.commandId),
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
                persistentProperties(message.commandId),
                payload
            )

            channel.waitForConfirmsOrDie()
        }
    }

    private fun persistentProperties(
        commandId: String
    ) =
        AMQP.BasicProperties.Builder()
            .contentType("application/json")
            .deliveryMode(2)
            .messageId(commandId)
            .build()
}