package com.notes.application.messaging

import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteMessage(
    val commandId: String,
    val title: String,
    val content: String
)