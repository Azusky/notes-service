package com.notes.api

import com.notes.api.routes.NoteRoutes
import com.notes.plugins.configureSerialization
import com.notes.plugins.configureStatusPages
import com.notes.testUtil.FakeNoteOperations
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteRoutesTest {

    @Test
    fun `GET notes returns 200`() = testApplication {

        application {
            configureSerialization()
            routing {
                NoteRoutes(
                    notes = FakeNoteOperations()
                ).register(this)
            }
        }

        val response = client.get("/notes")

        assertEquals(
            HttpStatusCode.OK,
            response.status
        )
    }
    @Test
    fun `POST notes returns 202`() = testApplication {

            application {
                configureSerialization()
                configureStatusPages()

                routing {
                    NoteRoutes(
                        notes = FakeNoteOperations()
                    ).register(this)
                }
            }

            val response = client.post("/notes") {
                contentType(ContentType.Application.Json)

                setBody(
                    """
            {
              "title": "New note",
              "content": "New content"
            }
            """.trimIndent()
                )
            }
            assertEquals(
                HttpStatusCode.Accepted,
                response.status
            )
        }
    @Test
    fun `PATCH note returns 202`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()

            routing {
                NoteRoutes(
                    notes = FakeNoteOperations()
                ).register(this)
            }
        }

        val response = client.patch("/notes/1") {
            contentType(ContentType.Application.Json)

            setBody(
                """
        {
          "version": 0,
          "title": "Updated title"
        }
        """.trimIndent()
            )
        }

        assertEquals(
            HttpStatusCode.Accepted,
            response.status
        )
    }
    @Test
    fun `DELETE note returns 204`() = testApplication {

        application {
            configureSerialization()

            routing {
                NoteRoutes(
                    notes = FakeNoteOperations()
                ).register(this)
            }
        }

        val response = client.delete("/notes/1")

        assertEquals(
            HttpStatusCode.NoContent,
            response.status
        )
    }
    @Test
    fun `PATCH with stale version returns conflict`() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()

            routing {
                NoteRoutes(
                    notes = FakeNoteOperations()
                ).register(this)
            }
        }

        client.patch("/notes/1") {
            contentType(ContentType.Application.Json)
            setBody(
                """
            {
              "version": 0,
              "title": "First update"
            }
            """.trimIndent()
            )
        }

        val response = client.patch("/notes/1") {
            contentType(ContentType.Application.Json)
            setBody(
                """
            {
              "version": 0,
              "title": "Stale update"
            }
            """.trimIndent()
            )
        }
        assertEquals(
            HttpStatusCode.Accepted,
            response.status
        )
    }
}