package com.notes.domain.exception

class TemporaryInfrastructureException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)