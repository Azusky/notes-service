package com.notes.testUtil


import com.notes.domain.result.Answer
import com.notes.domain.result.AnswerError

fun <T> Answer<T>.successData(): T =
    when (this) {
        is Answer.Success -> data
        is Answer.Error -> error(
            "Expected Success but got Error: $error"
        )
    }

fun <T> Answer<T>.errorData(): AnswerError =

    when (this) {

        is Answer.Success ->

            error("Expected Error but got Success")

        is Answer.Error ->

            error

    }