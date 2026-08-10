package com.notes.data

import com.notes.domain.model.Note
import com.notes.domain.repository.NoteRepository

class NoteRepositoryImpl : NoteRepository {
    private val notes = mutableListOf(
        Note(
            id = 1,
            title = "Learn Ktor",
            content = "Learn routing and serialization"
        ),

        Note(
            id = 2,
            title = "PostgreSQL",
            content = "Connect database later"
        )

    )
    override fun getAll(): List<Note> {
        return notes
    }
    override fun create(
        title: String,
        content: String
    ): Note {

        val note = Note(
            id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            content = content
        )

        notes.add(note)

        return note

    }

    override fun getById(id: Long): Note? {
        return notes.find { it.id == id }
    }

    override fun update(
        id: Long,
        title: String?,
        content: String?
    ): Note? {

        val index = notes.indexOfFirst { it.id == id }

        if (index == -1) {
            return null
        }

        val oldNote = notes[index]

        val updatedNote = oldNote.copy(
            title = title ?: oldNote.title,
            content = content ?: oldNote.content
        )

        notes[index] = updatedNote

        return updatedNote

    }

    override fun delete(id: Long): Boolean {
        return notes.removeIf { it.id == id }
    }
}