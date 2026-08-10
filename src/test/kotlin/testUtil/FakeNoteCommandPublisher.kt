package com.notes.testUtil

import com.notes.application.messaging.CreateNoteMessage
import com.notes.application.messaging.NoteCommandPublisher
import com.notes.application.messaging.UpdateNoteMessage

class FakeNoteCommandPublisher : NoteCommandPublisher {
    var lastPublished: CreateNoteMessage? = null
        private set

    override fun publishCreate(
        message: CreateNoteMessage
    ) {
        lastPublished = message
    }
    var lastUpdatePublished: UpdateNoteMessage? = null
        private set

    override fun publishUpdate(
        message: UpdateNoteMessage
    ) {
        lastUpdatePublished = message
    }
}