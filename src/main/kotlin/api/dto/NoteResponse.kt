package com.notes.api.dto

import kotlinx.serialization.Serializable


@Serializable
data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String,
    val version: Long
)