package com.notes.application.port

import com.notes.application.command.CreateNoteCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.model.Note

interface NoteOperations {

    fun getAll(): List<Note>

    fun getById(id: Long): Note

    fun create(command: CreateNoteCommand): Note

    fun update(command: UpdateNoteCommand): Note

    fun delete(id: Long)

}
