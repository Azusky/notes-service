package com.notes.domain.exception

class InvalidNoteIdException(
    message: String = "Invalid note id"
) : RuntimeException(message)