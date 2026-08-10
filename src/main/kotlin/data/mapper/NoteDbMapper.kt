package com.notes.data.mapper

import com.notes.data.database.NotesTable
import com.notes.domain.model.Note
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toNote(): Note =
    Note(
        id = this[NotesTable.id].value,
        title = this[NotesTable.title],
        content = this[NotesTable.content],
        version = this[NotesTable.version]
    )