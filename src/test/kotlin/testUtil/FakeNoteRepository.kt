package com.notes.testUtil

import com.notes.domain.model.Note
import com.notes.domain.repository.NoteRepository
import com.notes.domain.result.Answer
import com.notes.domain.result.AnswerError

class FakeNoteRepository : NoteRepository {

    private val notes = mutableListOf<Note>()
    private val processedCommands = mutableSetOf<String>()
    override fun getAll(): Answer<List<Note>> =
        Answer.Success(
            notes.toList()
        )

    override fun getById(id: Long): Answer<Note> {
        val note = notes.find { it.id == id }

        return if (note != null) {
            Answer.Success(note)
        } else {
            Answer.Error(
                AnswerError.NotFound
            )
        }
    }
    override fun createOnce(
        commandId: String,
        title: String,
        content: String
    ): Answer<Note> {

        if (commandId in processedCommands) {
            return Answer.Error(
                AnswerError.AlreadyProcessed
            )
        }

        val result = create(
            title = title,
            content = content
        )

        if (result is Answer.Success) {
            processedCommands.add(commandId)
        }

        return result
    }
    override fun create(
        title: String,
        content: String
    ): Answer<Note> {

        val note = Note(
            id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            content = content,
            version = 0
        )

        notes.add(note)

        return Answer.Success(note)
    }

    override fun update(
        id: Long,
        expectedVersion: Long,
        title: String?,
        content: String?
    ): Answer<Note> {

        val index = notes.indexOfFirst { it.id == id }

        if (index == -1) {
            return Answer.Error(
                AnswerError.NotFound
            )
        }

        val current = notes[index]

        if (current.version != expectedVersion) {
            return Answer.Error(
                AnswerError.VersionConflict
            )
        }

        val updated = current.copy(
            title = title ?: current.title,
            content = content ?: current.content,
            version = current.version + 1
        )

        notes[index] = updated

        return Answer.Success(updated)
    }

    override fun delete(id: Long): Answer<Unit> {

        val deleted = notes.removeIf {
            it.id == id
        }

        return if (deleted) {
            Answer.Success(Unit)
        } else {
            Answer.Error(
                AnswerError.NotFound
            )
        }
    }
}