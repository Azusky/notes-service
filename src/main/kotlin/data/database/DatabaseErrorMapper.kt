package com.notes.data.database

import com.notes.domain.result.AnswerError
import java.sql.SQLRecoverableException
import java.sql.SQLException
import java.sql.SQLTransientException

fun Throwable.toAnswerError(): AnswerError =
    if (isTemporaryDatabaseError()) {
        AnswerError.TemporaryInfrastructure
    } else {
        throw this
    }

private fun Throwable.isTemporaryDatabaseError(): Boolean {
    var current: Throwable? = this

    while (current != null) {
        when (current) {
            is SQLTransientException ->
                return true

            is SQLRecoverableException ->
                return true

            is SQLException -> {
                // SQLSTATE class 08 = connection exception
                if (current.sqlState?.startsWith("08") == true) {
                    return true
                }
            }
        }

        current = current.cause
    }

    return false
}