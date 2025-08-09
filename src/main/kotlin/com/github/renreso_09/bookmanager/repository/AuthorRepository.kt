package com.github.renreso_09.bookmanager.repository

import InternalException
import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.tables.records.AuthorsRecord
import org.jooq.impl.DSL.row

interface AuthorRepository {
    fun findByNames(names: List<String>): List<Author>
    fun bulkCreate(authors: List<Author>): List<AuthorId>
}

@Repository
class AuthorRepositoryImpl(private val dsl: DSLContext) : AuthorRepository {
    override fun findByNames(names: List<String>): List<Author> {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.NAME.`in`(names))
            .fetch()
            .map { record ->
                Author(
                    id = AuthorId(record.getValue(AUTHORS.ID)),
                    name = record.getValue(AUTHORS.NAME),
                    birthDate = record.getValue(AUTHORS.BIRTH_DATE)
                )
            }
    }

    override fun bulkCreate(authors: List<Author>): List<AuthorId> {
        if (authors.isEmpty()) return emptyList()

        val records = authors.map {
            AuthorsRecord().apply {
                name = it.name
                birthDate = it.birthDate
            }
        }
        val rows = records.map { record ->
            row(record.name, record.birthDate)
        }

        val insertedRecords = dsl.insertInto(AUTHORS, AUTHORS.NAME, AUTHORS.BIRTH_DATE)
            .valuesOfRows(rows)
            .returningResult(AUTHORS.ID)
            .fetch()
        return insertedRecords.map { record ->
            AuthorId(record.getValue(AUTHORS.ID))
        }
    }
}