package com.notes.domain.messaging

import com.notes.domain.model.Note

interface NotePublisher {
    fun publishCreated(note: Note)
}