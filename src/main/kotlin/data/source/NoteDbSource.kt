package com.notes.data.source

import com.notes.domain.model.Note
import com.notes.domain.result.Answer

interface NoteDbSource {

    fun getAll(): Answer<List<Note>>

    fun getById(
        id: Long
    ): Answer<Note>

    fun create(
        title: String,
        content: String
    ): Answer<Note>

    fun createOnce(
        commandId: String,
        title: String,
        content: String
    ): Answer<Note>

    fun update(
        id: Long,
        expectedVersion: Long,
        title: String?,
        content: String?
    ): Answer<Note>

    fun delete(
        id: Long
    ): Answer<Unit>
}