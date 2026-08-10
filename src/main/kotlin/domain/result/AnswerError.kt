package com.notes.domain.result

sealed interface AnswerError {

    data object NotFound : AnswerError

    data object VersionConflict : AnswerError

    data object InvalidData : AnswerError
    data object TemporaryInfrastructure : AnswerError
    data object AlreadyProcessed : AnswerError
}