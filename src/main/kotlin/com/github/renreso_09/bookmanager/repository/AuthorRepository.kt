package com.github.renreso_09.bookmanager.repository

import com.github.renreso_09.bookmanager.domain.model.Author
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS

interface AuthorRepository {
    fun create(author: Author): Author
     fun findByName(name: String): Author?
}

@Repository
class AuthorRepositoryImpl(private val dsl: DSLContext) : AuthorRepository {
    override fun create(author: Author): Author {
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDay)
            .returning(AUTHORS.ID)
            .fetchOne() ?: throw IllegalStateException("Failed to insert author")

        return author.copy(id = record.getValue(AUTHORS.ID))
    }

    override fun findByName(name: String): Author? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .fetchOne()?.let { record ->
                Author(
                    id = record.getValue(AUTHORS.ID),
                    name = record.getValue(AUTHORS.NAME),
                    birthDay = record.getValue(AUTHORS.BIRTH_DATE)
                )
            }
    }
}