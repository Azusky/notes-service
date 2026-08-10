package com.notes.application.messaging

interface NoteCommandPublisher {
    fun publishCreate(message: CreateNoteMessage)
    fun publishUpdate(
        message: UpdateNoteMessage
    )
}