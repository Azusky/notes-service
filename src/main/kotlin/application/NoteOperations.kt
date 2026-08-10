package com.notes.application

import com.notes.application.command.CreateNoteCommand
import com.notes.application.model.AcceptedCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.model.Note

interface NoteOperations {

    fun getAll(): List<Note>

    fun getById(id: Long): Note

    fun create(command: CreateNoteCommand): Note
    fun submitCreate(

        command: CreateNoteCommand

    ): AcceptedCommand
    fun update(command: UpdateNoteCommand): Note
    fun submitUpdate(
        command: UpdateNoteCommand
    ): AcceptedCommand
    fun delete(id: Long)

    fun createOnce(
        commandId: String,
        command: CreateNoteCommand
    ): Note?
}
