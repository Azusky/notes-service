package com.notes.domain.result

sealed interface Answer<out T> {

    data class Success<T>(

        val data: T

    ) : Answer<T>

    data class Error(

        val error: AnswerError

    ) : Answer<Nothing>

}