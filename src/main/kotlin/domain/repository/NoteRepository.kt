package com.notes.domain.repository

import com.notes.domain.model.Note

interface NoteRepository {
    fun getAll(): List<Note>
    fun create(title: String, content: String): Note
    fun getById(id: Long): Note?
    fun update(

        id: Long,

        title: String?,

        content: String?

    ): Note?
    fun delete(id: Long): Boolean
}