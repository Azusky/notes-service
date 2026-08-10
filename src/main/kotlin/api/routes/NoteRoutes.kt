package com.notes.api.routes

import com.notes.api.dto.CreateNoteRequest
import com.notes.api.dto.UpdateNoteRequest
import com.notes.api.mapper.toCommand
import com.notes.api.mapper.toResponse
import com.notes.application.NoteOperations
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

fun Route.noteRoutes(
    notes: NoteOperations) {
    route("/notes"){
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
                ?: throw BadRequestException("Invalid note id")
            val note = notes.getById(id)
            call.respond(note.toResponse())
        }

        post {
            val request = call.receive<CreateNoteRequest>()
            val note = notes.create(
                request.toCommand()
            )
            call.respond(
                status = HttpStatusCode.Created,
                message = note.toResponse()
            )
        }

        patch("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid note id")
            val request = call.receive<UpdateNoteRequest>()
            call.respond(
                notes.update(request.toCommand(id))
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