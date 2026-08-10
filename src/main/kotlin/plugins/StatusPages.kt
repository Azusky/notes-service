package com.notes.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf(
                    "error" to ( cause.message ?: "Invalid request" )
                )
            )
        }
    }
}
