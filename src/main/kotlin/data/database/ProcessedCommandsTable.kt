package com.notes.data.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
object ProcessedCommandsTable : Table("processed_commands") {

    val commandId = varchar(
        name = "command_id",
        length = 36
    )

    val processedAt = timestamp(
        name = "processed_at"
    )

    override val primaryKey = PrimaryKey(commandId)
}