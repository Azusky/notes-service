package com.notes.data.database
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object NotesTable : LongIdTable("notes")  {
    val title = varchar(
        name = "title",
        length = 255
    )
    val version = long("version").default(0)
    val content = text("content")
}