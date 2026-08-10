package com.notes.application

import com.notes.application.command.CreateNoteCommand
import com.notes.application.model.UpdateNoteCommand
import com.notes.domain.exception.ResourceNotFoundException
import com.notes.domain.exception.VersionConflictException
import com.notes.testUtil.FakeNoteCommandPublisher
import com.notes.testUtil.FakeNoteRepository
import com.notes.testUtil.successData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class NoteServiceTest {

    private fun createService(
        repository: FakeNoteRepository = FakeNoteRepository(),
        publisher: FakeNoteCommandPublisher = FakeNoteCommandPublisher()
    ): NoteService =
        NoteService(
            repository = repository,
            publisher = publisher
        )

    @Test
    fun `create returns generated id`() {
        val service = createService()

        val result = service.create(
            CreateNoteCommand(
                title = "Test title",
                content = "Test content"
            )
        )

        assertEquals(1, result.id)
    }

    @Test
    fun `create returns title`() {
        val service = createService()

        val result = service.create(
            CreateNoteCommand(
                title = "Test title",
                content = "Test content"
            )
        )

        assertEquals(
            "Test title",
            result.title
        )
    }

    @Test
    fun `create returns content`() {
        val service = createService()

        val result = service.create(
            CreateNoteCommand(
                title = "Test title",
                content = "Test content"
            )
        )

        assertEquals(
            "Test content",
            result.content
        )
    }

    @Test
    fun `create returns initial version zero`() {
        val service = createService()

        val result = service.create(
            CreateNoteCommand(
                title = "Test title",
                content = "Test content"
            )
        )

        assertEquals(
            0,
            result.version
        )
    }

    @Test
    fun `getById returns existing note`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Title",
            content = "Content"
        ).successData()

        val result = service.getById(
            created.id
        )

        assertEquals(
            created,
            result
        )
    }

    @Test
    fun `getById throws when note does not exist`() {
        val service = createService()

        assertFailsWith<ResourceNotFoundException> {
            service.getById(999)
        }
    }

    @Test
    fun `update changes provided title`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        val result = service.update(
            UpdateNoteCommand(
                id = created.id,
                expectedVersion = created.version,
                title = "New title",
                content = null
            )
        )

        assertEquals(
            "New title",
            result.title
        )
    }

    @Test
    fun `update preserves content when content is not provided`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        val result = service.update(
            UpdateNoteCommand(
                id = created.id,
                expectedVersion = created.version,
                title = "New title",
                content = null
            )
        )

        assertEquals(
            "Old content",
            result.content
        )
    }

    @Test
    fun `update increments version`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        val result = service.update(
            UpdateNoteCommand(
                id = created.id,
                expectedVersion = created.version,
                title = "New title",
                content = null
            )
        )

        assertEquals(
            created.version + 1,
            result.version
        )
    }

    @Test
    fun `update throws when version is stale`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Old title",
            content = "Old content"
        ).successData()

        service.update(
            UpdateNoteCommand(
                id = created.id,
                expectedVersion = created.version,
                title = "First update",
                content = null
            )
        )

        assertFailsWith<VersionConflictException> {
            service.update(
                UpdateNoteCommand(
                    id = created.id,
                    expectedVersion = created.version,
                    title = "Stale update",
                    content = null
                )
            )
        }
    }

    @Test
    fun `delete removes note`() {
        val repository = FakeNoteRepository()
        val service = createService(
            repository = repository
        )

        val created = repository.create(
            title = "Title",
            content = "Content"
        ).successData()

        service.delete(created.id)

        assertFailsWith<ResourceNotFoundException> {
            service.getById(created.id)
        }
    }

    @Test
    fun `submitCreate publishes create command`() {
        val publisher = FakeNoteCommandPublisher()

        val service = createService(
            publisher = publisher
        )

        service.submitCreate(
            CreateNoteCommand(
                title = "Async title",
                content = "Async content"
            )
        )

        assertEquals(
            "Async title",
            publisher.lastPublished?.title
        )
    }

    @Test
    fun `submitCreate publishes content`() {
        val publisher = FakeNoteCommandPublisher()

        val service = createService(
            publisher = publisher
        )

        service.submitCreate(
            CreateNoteCommand(
                title = "Async title",
                content = "Async content"
            )
        )

        assertEquals(
            "Async content",
            publisher.lastPublished?.content
        )
    }

    @Test
    fun `submitCreate generates command id`() {
        val publisher = FakeNoteCommandPublisher()

        val service = createService(
            publisher = publisher
        )

        service.submitCreate(
            CreateNoteCommand(
                title = "Async title",
                content = "Async content"
            )
        )

        assertNotNull(
            publisher.lastPublished?.commandId
        )
    }

    @Test
    fun `submitCreate returns published command id`() {
        val publisher = FakeNoteCommandPublisher()

        val service = createService(
            publisher = publisher
        )

        val accepted = service.submitCreate(
            CreateNoteCommand(
                title = "Async title",
                content = "Async content"
            )
        )

        assertEquals(
            publisher.lastPublished?.commandId,
            accepted.commandId
        )
    }
}