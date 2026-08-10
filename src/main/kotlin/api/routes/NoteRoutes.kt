package com.notes.api.routes

import com.notes.api.dto.CreateNoteRequest
import com.notes.api.dto.UpdateNoteRequest
import com.notes.api.mapper.toCommand
import com.notes.api.mapper.toResponse
import com.notes.application.NoteOperations
import com.notes.domain.exception.InvalidNoteIdException
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.text.toLongOrNull

class NoteRoutes(
    private val notes: NoteOperations
) {
    fun register(route: Route) {

        route.route("/notes"){
            get {
                val result = notes.getAll()
                    .map { it.toResponse() }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = result
                )
            }
            get("/{id}") {

                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw InvalidNoteIdException()
                val note = notes.getById(id)
                call.respond(note.toResponse())
            }

            post {
                val request = call.receive<CreateNoteRequest>()
                val accepted = notes.submitCreate(
                    request.toCommand()
                )
                call.respond(
                    status = HttpStatusCode.Accepted,
                    message = accepted
                )
            }

            patch("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid note id")
                val request = call.receive<UpdateNoteRequest>()
                val accepted = notes.submitUpdate(
                    request.toCommand(id)
                )
                call.respond(
                    status = HttpStatusCode.Accepted,
                    message = accepted
                )
            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid note id")
                notes.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }

    }
}
