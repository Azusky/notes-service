package com.notes.plugins

import io.ktor.server.application.*
import dev.hayden.KHealth

fun Application.configureMonitoring() {
    install(KHealth)
}