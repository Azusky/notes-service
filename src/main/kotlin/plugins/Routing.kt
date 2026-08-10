package com.notes.plugins

import com.notes.api.routes.NoteRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    val noteRoutes = get<NoteRoutes>()
    routing {
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi/documentation.yaml"
        )
        noteRoutes.register(this)
    }
}