package com.notes.application.model

import kotlinx.serialization.Serializable

@Serializable
data class AcceptedCommand(
    val commandId: String
)