package com.notes.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteRequest(
    val version: Long,
    val title: String? = null,
    val content: String? = null
)