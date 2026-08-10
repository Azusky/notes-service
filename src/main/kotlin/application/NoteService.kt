package com.notes.application

import com.notes.application.command.CreateNoteCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.exception.ResourceNotFoundException
import com.notes.domain.model.Note
import com.notes.domain.repository.NoteRepository

class NoteService(
    private val repository: NoteRepository
) : NoteOperations {

    override fun getAll(): List<Note> {
        return repository.getAll()
    }

    override fun getById(id: Long): Note {
        return repository.getById(id)
            ?: throw ResourceNotFoundException("Note not found")
    }

    override fun create(command: CreateNoteCommand): Note {
        require(command.title.isNotBlank()) {
            "Title cannot be empty"
        }

        require(command.content.isNotBlank()) {
            "Content cannot be empty"
        }

        return repository.create(
            title = command.title,
            content = command.content
        )
    }

    override fun update(command: UpdateNoteCommand): Note {
        require(command.title != null || command.content != null) {
            "Nothing to update"
        }

        command.title?.let {
            require(it.isNotBlank()) {
                "Title cannot be empty"
            }
        }

        command.content?.let {
            require(it.isNotBlank()) {
                "Content cannot be empty"
            }
        }

        return repository.update(
            id = command.id,
            title = command.title,
            content = command.content
        ) ?: throw ResourceNotFoundException("Note not found")
    }

    override fun delete(id: Long) {
        val deleted = repository.delete(id)

        if (!deleted) {
            throw ResourceNotFoundException("Note not found")
        }
    }
}