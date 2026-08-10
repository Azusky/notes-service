package com.notes.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: Long,
    val title: String,
    val content: String
)