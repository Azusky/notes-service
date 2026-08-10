package com.notes.application

import com.notes.application.command.CreateNoteCommand
import com.notes.application.messaging.CreateNoteMessage
import com.notes.application.messaging.NoteCommandPublisher
import com.notes.application.messaging.UpdateNoteMessage
import com.notes.application.model.AcceptedCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.exception.InvalidDataException
import com.notes.domain.exception.ResourceNotFoundException
import com.notes.domain.exception.TemporaryInfrastructureException
import com.notes.domain.exception.VersionConflictException
import com.notes.domain.model.Note
import com.notes.domain.repository.NoteRepository
import com.notes.domain.result.Answer
import com.notes.domain.result.AnswerError
import java.util.UUID

class NoteService(
    private val repository: NoteRepository,
    private val publisher: NoteCommandPublisher
) : NoteOperations {

    override fun getAll(): List<Note> =
        when (val result = repository.getAll()) {
            is Answer.Success -> result.data

            is Answer.Error -> throw IllegalStateException(
                "Unexpected error: ${result.error}"
            )
        }

    override fun getById(id: Long): Note =
        when (val result = repository.getById(id)) {
            is Answer.Success -> result.data

            is Answer.Error -> throw result.error.toException()
        }

    override fun create(command: CreateNoteCommand): Note {
        require(command.title.isNotBlank()) {
            "Title cannot be empty"
        }

        require(command.content.isNotBlank()) {
            "Content cannot be empty"
        }

        return when (
            val result = repository.create(
                title = command.title,
                content = command.content
            )
        ) {
            is Answer.Success -> result.data

            is Answer.Error ->
                throw IllegalStateException(
                    "Unexpected error: ${result.error}"
                )
        }
    }
    override fun createOnce(
        commandId: String,
        command: CreateNoteCommand
    ): Note? {

        require(command.title.isNotBlank()) {
            "Title cannot be empty"
        }

        require(command.content.isNotBlank()) {
            "Content cannot be empty"
        }

        return when (
            val result = repository.createOnce(
                commandId = commandId,
                title = command.title,
                content = command.content
            )
        ) {
            is Answer.Success ->
                result.data

            is Answer.Error ->
                when (result.error) {
                    AnswerError.AlreadyProcessed ->
                        null

                    else ->
                        throw result.error.toException()
                }
        }
    }
    override fun submitCreate(
        command: CreateNoteCommand
    ): AcceptedCommand {
        require(command.title.isNotBlank()) {
            "Title cannot be empty"
        }
        require(command.content.isNotBlank()) {
            "Content cannot be empty"
        }
        val commandId = UUID.randomUUID().toString()
        publisher.publishCreate(
            CreateNoteMessage(
                commandId = commandId,
                title = command.title,
                content = command.content
            )
        )
        return AcceptedCommand(
            commandId = commandId
        )
    }

    override fun update(command: UpdateNoteCommand): Note {
        require(command.title != null || command.content != null) {
            "Nothing to update"
        }

        val result = repository.update(
            id = command.id,
            expectedVersion = command.expectedVersion,
            title = command.title,
            content = command.content
        )

        return when (result) {
            is Answer.Success -> result.data

            is Answer.Error -> throw result.error.toException()
        }
    }
    override fun submitUpdate(
        command: UpdateNoteCommand
    ): AcceptedCommand {

        require(
            command.title != null ||
                    command.content != null
        ) {
            "Nothing to update"
        }

        val commandId = UUID.randomUUID().toString()

        publisher.publishUpdate(
            UpdateNoteMessage(
                commandId = commandId,
                id = command.id,
                expectedVersion = command.expectedVersion,
                title = command.title,
                content = command.content
            )
        )

        return AcceptedCommand(
            commandId = commandId
        )
    }

    override fun delete(id: Long) {
        when (val result = repository.delete(id)) {
            is Answer.Success -> Unit

            is Answer.Error -> throw result.error.toException()
        }
    }
    private fun AnswerError.toException(): RuntimeException =
        when (this) {
            AnswerError.NotFound -> ResourceNotFoundException("Note not found")
            AnswerError.VersionConflict -> VersionConflictException("Note version is outdated")
            AnswerError.InvalidData -> InvalidDataException("Invalid data")
            AnswerError.TemporaryInfrastructure ->
                TemporaryInfrastructureException(
                    "Infrastructure temporarily unavailable"
                )
            AnswerError.AlreadyProcessed ->
                IllegalStateException(
                    "Command already processed"
                )
        }
}