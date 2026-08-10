package com.notes.plugins

import com.notes.domain.exception.InvalidDataException
import com.notes.domain.exception.InvalidNoteIdException
import com.notes.domain.exception.ResourceNotFoundException
import com.notes.domain.exception.VersionConflictException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*


fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<InvalidNoteIdException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to (cause.message ?: "Invalid note id")
                )
            )
        }

        exception<InvalidDataException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to (cause.message ?: "Invalid data")
                )
            )
        }

        exception<ResourceNotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf(
                    "error" to (cause.message ?: "Resource not found")
                )
            )
        }

        exception<VersionConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                mapOf(
                    "error" to (cause.message ?: "Version conflict")
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            call.application.log.warn(
                "Bad request",
                cause
            )

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Invalid request body"
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to (cause.message ?: "Invalid request")
                )
            )
        }

        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Invalid request body"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error(
                "Unhandled exception",
                cause
            )

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(
                    "error" to "Internal server error"
                )
            )
        }
    }
}