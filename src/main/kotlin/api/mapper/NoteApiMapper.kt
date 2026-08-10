package com.notes.api.mapper
import com.notes.api.dto.CreateNoteRequest

import com.notes.api.dto.NoteResponse

import com.notes.api.dto.UpdateNoteRequest

import com.notes.application.command.CreateNoteCommand

import com.notes.application.model.UpdateNoteCommand

import com.notes.domain.model.Note

fun CreateNoteRequest.toCommand(): CreateNoteCommand {
    return CreateNoteCommand(
        title = title,
        content = content
    )
}

fun UpdateNoteRequest.toCommand(id: Long): UpdateNoteCommand {
    return UpdateNoteCommand(
        id = id,
        title = title,
        content = content,
        expectedVersion = version
    )
}

fun Note.toResponse(): NoteResponse =
    NoteResponse(
        id = id,
        title = title,
        content = content,
        version = version
    )