package com.notes.data.messaging

import com.notes.application.NoteOperations
import com.notes.application.messaging.UpdateNoteMessage
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.exception.TemporaryInfrastructureException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery
import kotlinx.serialization.json.Json

class UpdateNoteWorker(
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
            RabbitMqTopology.UPDATE_QUEUE,
            false,
            { _, delivery ->

                try {
                    val message =
                        Json.decodeFromString<UpdateNoteMessage>(
                            delivery.body.decodeToString()
                        )

                    notes.update(
                        UpdateNoteCommand(
                            id = message.id,
                            expectedVersion = message.expectedVersion,
                            title = message.title,
                            content = message.content
                        )
                    )

                    channel.basicAck(
                        delivery.envelope.deliveryTag,
                        false
                    )

                }catch (exception: TemporaryInfrastructureException) {
                    retryHandler.retry(
                        channel = channel,
                        delivery = delivery,
                        retryRoutingKey = RabbitMqTopology.UPDATE_RETRY_ROUTING_KEY,
                        dlqRoutingKey = RabbitMqTopology.UPDATE_DLQ_ROUTING_KEY
                    )
                } catch (exception: Exception) {
                    retryHandler.deadLetter(
                        channel = channel,
                        delivery = delivery,
                        dlqRoutingKey = RabbitMqTopology.UPDATE_DLQ_ROUTING_KEY
                    )
                }
            },
            { }
        )
    }

}