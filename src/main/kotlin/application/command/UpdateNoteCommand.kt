package com.notes.application.model


data class UpdateNoteCommand(
    val id: Long,
    val expectedVersion: Long,
    val title: String?,
    val content: String?
)