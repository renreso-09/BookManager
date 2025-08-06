package com.github.renreso_09.bookmanager.repository

import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.tables.records.AuthorsRecord

interface AuthorRepository {
    fun create(author: Author): Author
    fun findByNames(names: List<String>): List<Author>
    fun bulkCreate(authors: List<Author>): List<AuthorId>
}

@Repository
class AuthorRepositoryImpl(private val dsl: DSLContext) : AuthorRepository {
    override fun create(author: Author): Author {
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDay)
            .returning(AUTHORS.ID)
            .fetchOne() ?: throw IllegalStateException("Failed to insert author")

        return Author(
            id = AuthorId(record.getValue(AUTHORS.ID)),
            name = record.getValue(AUTHORS.NAME),
            birthDay = record.getValue(AUTHORS.BIRTH_DATE)
        )
    }

    override fun findByNames(names: List<String>): List<Author> {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.NAME.`in`(names))
            .fetch()
            .map { record ->
                Author(
                    id = AuthorId(record.getValue(AUTHORS.ID)),
                    name = record.getValue(AUTHORS.NAME),
                    birthDay = record.getValue(AUTHORS.BIRTH_DATE)
                )
            }
    }

    override fun bulkCreate(authors: List<Author>): List<AuthorId> {
        if (authors.isEmpty()) return emptyList()

        val records = authors.map { author ->
            AuthorsRecord().apply {
                name = author.name
                birthDate = author.birthDay
            }
        }
        val insertedRecords = dsl.batchInsert(records).execute()
        return insertedRecords.map { record ->
            AuthorId(record)
        }
    }
}