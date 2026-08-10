package com.notes.data

import com.notes.data.database.NotesTable
import com.notes.data.source.PostgresNoteDbSource
import com.notes.domain.result.Answer
import com.notes.domain.result.AnswerError
import com.notes.testUtil.errorData
import com.notes.testUtil.successData
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.plugins.NotFoundException
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID
import kotlin.test.assertEquals


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresNoteDbSourceTest {
    private lateinit var dataSource: HikariDataSource
    @BeforeAll
    fun setupSchema() {
         dataSource = HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = "jdbc:postgresql://localhost:5432/notes_test"
                username = "postgres"
                password = "postgres"
                maximumPoolSize = 2
                isAutoCommit = false
            }
        )

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load()
            .apply {
                clean()
                migrate()
            }
        Database.connect(dataSource)
    }
    @BeforeEach
    fun cleanData() {
        transaction {
            NotesTable.deleteAll()
        }
    }

    @AfterAll
    fun closeDatabase() {
        dataSource.close()
    }

    @Test
    fun `create and getById persist note in postgres`() {
        val source = PostgresNoteDbSource()
        val created = source.create(
            title = "Integration test",
            content = "Stored in Postgres"
        ).successData()
        val loaded = source.getById(created.id).successData()
        assertEquals(created, loaded)
    }
    @Test
    fun `createOnce creates only one note for duplicate command id`() {
        val source = PostgresNoteDbSource()

        val commandId = "duplicate-command-id"

        source.createOnce(
            commandId = commandId,
            title = "Idempotent note",
            content = "Content"
        )

        source.createOnce(
            commandId = commandId,
            title = "Idempotent note",
            content = "Content"
        )

        val notes = source
            .getAll()
            .successData()

        assertEquals(
            1,
            notes.size
        )
    }
    @Test
    fun `createOnce returns already processed for duplicate command id`() {
        val source = PostgresNoteDbSource()
        val commandId = UUID.randomUUID().toString()

        source.createOnce(
            commandId = commandId,
            title = "Idempotent note",
            content = "Content"
        )

        val second = source.createOnce(
            commandId = commandId,
            title = "Idempotent note",
            content = "Content"
        )

        assertEquals(
            Answer.Error(AnswerError.AlreadyProcessed),
            second
        )
    }
    @Test
    fun `update changes provided title`() {
        val source = PostgresNoteDbSource()

        val created = source.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        val updated = source.update(
            id = created.id,
            title = "New title",
            content = null,
            expectedVersion = created.version
        ).successData()

        assertEquals("New title", updated.title)
    }
    @Test
    fun `update preserves content when content is not provided`() {
        val source = PostgresNoteDbSource()

        val created = source.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        val updated = source.update(
            id = created.id,
            title = "New title",
            content = null,
            expectedVersion = created.version
        ).successData()

        assertEquals("Old content", updated.content)
    }

    @Test
    fun `delete returns success when note exists`() {
        val source = PostgresNoteDbSource()

        val created = source.create(
            title = "Delete me",
            content = "Temporary note"
        ).successData()

        val result = source
            .delete(created.id)
            .successData()

        assertEquals(Unit, result)
    }
    @Test
    fun `deleted note cannot be loaded`() {
        val source = PostgresNoteDbSource()

        val created = source.create(
            title = "Delete me",
            content = "Temporary note"
        ).successData()

        source.delete(created.id)

        val loaded = source.getById(created.id)

        assertEquals(
            AnswerError.NotFound,
            loaded.errorData()
        )
    }
    @Test
    fun `delete returns not found when note does not exist`() {
        val source = PostgresNoteDbSource()

        val error = source
            .delete(999999)
            .errorData()

        assertEquals(
            AnswerError.NotFound,
            error
        )
    }
}