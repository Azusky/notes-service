package com.notes.data

import com.notes.data.source.NoteDbSource
import com.notes.domain.model.Note
import com.notes.domain.repository.NoteRepository
import com.notes.domain.result.Answer

class NoteRepositoryImpl(
    private val dbSource: NoteDbSource
) : NoteRepository {

    override fun getAll(): Answer<List<Note>> =
        dbSource.getAll()

    override fun getById(id: Long): Answer<Note> =
        dbSource.getById(id)

    override fun create(
        title: String,
        content: String
    ): Answer<Note> =
        dbSource.create(
            title = title,
            content = content
        )
    override fun createOnce(
        commandId: String,
        title: String,
        content: String
    ): Answer<Note> =
        dbSource.createOnce(
            commandId = commandId,
            title = title,
            content = content
        )
    override fun update(
        id: Long,
        expectedVersion: Long,
        title: String?,
        content: String?
    ): Answer<Note> =
        dbSource.update(
            id = id,
            expectedVersion = expectedVersion,
            title = title,
            content = content
        )

    override fun delete(id: Long): Answer<Unit> =
        dbSource.delete(id)
}