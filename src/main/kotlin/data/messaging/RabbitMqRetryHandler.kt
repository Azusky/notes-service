package com.notes.data.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Delivery

class RabbitMqRetryHandler {

    fun retry(
        channel: Channel,
        delivery: Delivery,
        retryRoutingKey: String,
        dlqRoutingKey: String
    ) {
        val currentRetryCount =
            (delivery.properties.headers?.get(RETRY_HEADER) as? Int)
                ?: 0

        if (currentRetryCount >= MAX_RETRIES) {
            deadLetter(
                channel = channel,
                delivery = delivery,
                dlqRoutingKey = dlqRoutingKey
            )
            return
        }

        val headers = delivery.properties.headers
            ?.toMutableMap()
            ?: mutableMapOf()

        headers[RETRY_HEADER] = currentRetryCount + 1

        val properties = delivery.properties
            .builder()
            .headers(headers)
            .deliveryMode(2)
            .build()

        channel.basicPublish(
            RabbitMqTopology.COMMAND_EXCHANGE,
            retryRoutingKey,
            properties,
            delivery.body
        )

        channel.waitForConfirmsOrDie()

        channel.basicAck(
            delivery.envelope.deliveryTag,
            false
        )
    }

    fun deadLetter(
        channel: Channel,
        delivery: Delivery,
        dlqRoutingKey: String
    ) {
        channel.basicPublish(
            RabbitMqTopology.COMMAND_EXCHANGE,
            dlqRoutingKey,
            delivery.properties,
            delivery.body
        )

        channel.waitForConfirmsOrDie()

        channel.basicAck(
            delivery.envelope.deliveryTag,
            false
        )
    }

    private companion object {
        const val RETRY_HEADER = "x-retry-count"
        const val MAX_RETRIES = 3
    }
}