package com.notes.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database


object DatabaseFactory {

    fun init(
        jdbcUrl: String,
        username: String,
        password: String
    ) {

        val dataSource = HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                this.jdbcUrl = jdbcUrl
                this.username = username
                this.password = password
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
        )
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        Database.connect(dataSource)

    }

}