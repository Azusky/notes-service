package com.notes.testUtil

import com.notes.application.NoteOperations
import com.notes.application.command.CreateNoteCommand
import com.notes.application.messaging.CreateNoteMessage
import com.notes.application.model.AcceptedCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.exception.ResourceNotFoundException
import com.notes.domain.exception.VersionConflictException
import com.notes.domain.model.Note

class FakeNoteOperations : NoteOperations {


    private val notes = mutableListOf(
        Note(
            id = 1,
            title = "Test title",
            content = "Test content",
            version = 0
        )
    )

    override fun getAll(): List<Note> = notes

    override fun getById(id: Long): Note {

        return notes.first { it.id == id }

    }
    override fun createOnce(
        commandId: String,
        command: CreateNoteCommand
    ): Note? {
        return create(command)
    }
    override fun create(command: CreateNoteCommand): Note {

        val note = Note(
            id = 2,
            title = command.title,
            content = command.content,
            version = 0,
        )

        notes.add(note)

        return note

    }
    override fun submitCreate(
        command: CreateNoteCommand
    ): AcceptedCommand {
        return AcceptedCommand(
            commandId = "test-command-id"
        )
    }


    override fun update(command: UpdateNoteCommand): Note {
        val index = notes.indexOfFirst { it.id == command.id }

        if (index == -1) {
            throw ResourceNotFoundException("Note not found")
        }

        val current = notes[index]

        if (current.version != command.expectedVersion) {
            throw VersionConflictException(
                "Note version is outdated"
            )
        }

        val updated = current.copy(
            title = command.title ?: current.title,
            content = command.content ?: current.content,
            version = current.version + 1
        )

        notes[index] = updated

        return updated
    }
    override fun submitUpdate(
        command: UpdateNoteCommand
    ): AcceptedCommand =
        AcceptedCommand(
            commandId = "test-update-command-id"
        )
    override fun delete(id: Long) {
        notes.removeIf { it.id == id }
    }

}