package com.notes.plugins

import com.notes.api.routes.noteRoutes
import com.notes.data.NoteRepositoryImpl
import com.notes.application.NoteService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val repository = NoteRepositoryImpl()
    val service = NoteService(repository)

    routing {
        noteRoutes(service)
    }
}