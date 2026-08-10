package com.notes.application.messaging

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteMessage(
    val commandId: String,
    val id: Long,
    val expectedVersion: Long,
    val title: String?,
    val content: String?
)