package com.github.renreso_09.bookmanager.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS

interface BookAuthorRelationRepository {
    fun create(bookId: Int, authorId: Int): Boolean
}

@Repository
class BookAuthorRelationRepositoryImpl(private val dsl: DSLContext) : BookAuthorRelationRepository {
    override fun create(bookId: Int, authorId: Int): Boolean {
        val result = dsl.insertInto(BOOK_AUTHOR_RELATIONS)
            .set(BOOK_AUTHOR_RELATIONS.BOOK_ID, bookId)
            .set(BOOK_AUTHOR_RELATIONS.AUTHOR_ID, authorId)
            .execute()
        return result > 0
    }
}