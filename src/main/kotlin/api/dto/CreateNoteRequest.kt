package com.notes.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateNoteRequest(
    val title: String,
    val content: String
)