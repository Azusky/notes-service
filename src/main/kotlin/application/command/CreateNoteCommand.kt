package com.notes.application.command

data class CreateNoteCommand(
    val title: String,
    val content: String
)