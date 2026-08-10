package com.notes.di

import com.notes.api.routes.NoteRoutes
import com.notes.application.NoteOperations
import com.notes.application.NoteService
import com.notes.application.messaging.NoteCommandPublisher
import com.notes.data.NoteRepositoryImpl
import com.notes.data.messaging.CreateNoteWorker
import com.notes.data.messaging.RabbitMqConfig
import com.notes.data.messaging.RabbitMqConnection
import com.notes.data.messaging.RabbitMqNoteCommandPublisher
import com.notes.data.messaging.RabbitMqRetryHandler
import com.notes.data.messaging.UpdateNoteWorker
import com.notes.data.source.NoteDbSource
import com.notes.data.source.PostgresNoteDbSource
import com.notes.domain.repository.NoteRepository
import org.koin.dsl.module

fun appModule( rabbitMqConfig: RabbitMqConfig) = module {
    single {
        RabbitMqRetryHandler()
    }
    single {
        CreateNoteWorker(
            connection = get(),
            notes = get(),
            retryHandler = get()
        )
    }
    single {
        UpdateNoteWorker(
            connection = get(),
            notes = get(),
            retryHandler = get()
        )
    }

    single {
        RabbitMqConnection(
            host = rabbitMqConfig.host,
            port = rabbitMqConfig.port,
            username = rabbitMqConfig.username,
            password = rabbitMqConfig.password
        )
    }
    single<NoteCommandPublisher> {
        RabbitMqNoteCommandPublisher(
            connection = get()
        )
    }
    single<NoteDbSource> {
        PostgresNoteDbSource()
    }
    single<NoteRepository> {
        NoteRepositoryImpl(
            dbSource = get()
        )
    }
    single<NoteOperations> (){
        NoteService(get(),get())
    }
    single {
        NoteRoutes(get())
    }
}