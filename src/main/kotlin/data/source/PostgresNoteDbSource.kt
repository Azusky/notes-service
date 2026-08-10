package com.notes.data.source

import com.notes.data.database.NotesTable
import com.notes.data.database.ProcessedCommandsTable
import com.notes.data.database.toAnswerError
import com.notes.data.mapper.toNote
import com.notes.domain.model.Note
import com.notes.domain.result.Answer
import com.notes.domain.result.AnswerError
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class PostgresNoteDbSource : NoteDbSource {

    override fun getAll(): Answer<List<Note>> =
        transaction {
            val notes = NotesTable
                .selectAll()
                .map { it.toNote() }

            Answer.Success(notes)
        }

    override fun getById(id: Long): Answer<Note> =
        transaction {
            val note = NotesTable
                .selectAll()
                .where { NotesTable.id eq id }
                .singleOrNull()
                ?.toNote()

            if (note != null) {
                Answer.Success(note)
            } else {
                Answer.Error(AnswerError.NotFound)
            }
        }
    override fun create(
        title: String,
        content: String
    ): Answer<Note> =
        try {
            transaction {
                val id = NotesTable.insertAndGetId {
                    it[NotesTable.title] = title
                    it[NotesTable.content] = content
                }.value

                val note = NotesTable
                    .selectAll()
                    .where { NotesTable.id eq id }
                    .single()
                    .toNote()

                Answer.Success(note)
            }
        } catch (exception: Exception) {
            Answer.Error(
                exception.toAnswerError()
            )
        }
    override fun createOnce(
        commandId: String,
        title: String,
        content: String
    ): Answer<Note> =
        try {
            transaction {
                val alreadyProcessed = ProcessedCommandsTable
                    .selectAll()
                    .where {
                        ProcessedCommandsTable.commandId eq commandId
                    }
                    .any()

                if (alreadyProcessed) {
                    return@transaction Answer.Error(
                        AnswerError.AlreadyProcessed
                    )
                }

                val noteId = NotesTable.insertAndGetId {
                    it[NotesTable.title] = title
                    it[NotesTable.content] = content
                }.value

                ProcessedCommandsTable.insert {
                    it[ProcessedCommandsTable.commandId] = commandId
                }

                val note = NotesTable
                    .selectAll()
                    .where { NotesTable.id eq noteId }
                    .single()
                    .toNote()

                Answer.Success(note)
            }
        } catch (exception: Exception) {
            Answer.Error(
                exception.toAnswerError()
            )
        }
    override fun update(
        id: Long,
        expectedVersion: Long,
        title: String?,
        content: String?
    ): Answer<Note> =
        transaction {
            val updatedRows = NotesTable.update(
                where = {
                    (NotesTable.id eq id) and
                            (NotesTable.version eq expectedVersion)
                }
            ) {
                title?.let { value ->
                    it[NotesTable.title] = value
                }

                content?.let { value ->
                    it[NotesTable.content] = value
                }

                it[NotesTable.version] =
                    NotesTable.version.nextVersion()
            }

            if (updatedRows == 0) {
                val exists = NotesTable
                    .selectAll()
                    .where { NotesTable.id eq id }
                    .any()

                return@transaction if (exists) {
                    Answer.Error(
                        AnswerError.VersionConflict
                    )
                } else {
                    Answer.Error(
                        AnswerError.NotFound
                    )
                }
            }

            val note = NotesTable
                .selectAll()
                .where { NotesTable.id eq id }
                .single()
                .toNote()

            Answer.Success(note)
        }

    override fun delete(id: Long): Answer<Unit> =
        transaction {
            val deletedRows = NotesTable.deleteWhere {
                NotesTable.id eq id
            }

            if (deletedRows > 0) {
                Answer.Success(Unit)
            } else {
                Answer.Error(
                    AnswerError.NotFound
                )
            }
        }

    private fun Column<Long>.nextVersion(): Expression<Long> =
        this + 1L
}