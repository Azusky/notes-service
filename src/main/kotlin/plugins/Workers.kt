package com.notes.plugins
import com.notes.data.messaging.CreateNoteWorker
import com.notes.data.messaging.UpdateNoteWorker
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configureWorkers() {
    val createNoteWorker = get<CreateNoteWorker>()
    val updateNoteWorker = get<UpdateNoteWorker>()

    createNoteWorker.start()
    updateNoteWorker.start()
}