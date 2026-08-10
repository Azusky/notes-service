package com.notes.data.messaging

import com.notes.application.NoteOperations
import com.notes.application.command.CreateNoteCommand
import com.notes.application.messaging.CreateNoteMessage
import com.notes.domain.exception.TemporaryInfrastructureException
import kotlinx.serialization.json.Json
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery

class CreateNoteWorker(
    private val connection: RabbitMqConnection,
    private val notes: NoteOperations,
    private val retryHandler: RabbitMqRetryHandler
) {

    fun start() {
        val channel = connection.createChannel()

        RabbitMqTopology.declare(channel)
        channel.confirmSelect()
        channel.basicQos(1)
        channel.basicConsume(
            RabbitMqTopology.CREATE_QUEUE,
            false,
            { _, delivery ->

                val deliveryTag = delivery.envelope.deliveryTag
                try {
                    val message = Json.decodeFromString<CreateNoteMessage>(
                        delivery.body.decodeToString()
                    )
                    notes.createOnce(
                        commandId = message.commandId,
                        command = CreateNoteCommand(
                            title = message.title,
                            content = message.content
                        )
                    )

                    channel.basicAck(
                        deliveryTag,
                        false
                    )

                } catch (exception: TemporaryInfrastructureException) {
                    retryHandler.retry(
                        channel = channel,
                        delivery = delivery,
                        retryRoutingKey = RabbitMqTopology.CREATE_RETRY_ROUTING_KEY,
                        dlqRoutingKey = RabbitMqTopology.CREATE_DLQ_ROUTING_KEY
                    )

                } catch (exception: Exception) {
                    retryHandler.deadLetter(
                        channel = channel,
                        delivery = delivery,
                        dlqRoutingKey = RabbitMqTopology.CREATE_DLQ_ROUTING_KEY
                    )
                }
            },
            { }
        )
    }
}