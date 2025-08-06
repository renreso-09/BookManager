package com.github.renreso_09.bookmanager.repository

import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.domain.model.BookId
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS

interface BookAuthorRelationRepository {
    fun create(bookId: Int, authorId: Int): Boolean
    fun batchCreateByBookId(bookId: BookId, authorIds: List<AuthorId>): Boolean
    fun deleteByBookId(bookId: Int): Boolean
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

    override fun deleteByBookId(bookId: Int): Boolean {
        val result = dsl.deleteFrom(BOOK_AUTHOR_RELATIONS)
            .where(BOOK_AUTHOR_RELATIONS.BOOK_ID.eq(bookId))
            .execute()
        return result > 0
    }

    override fun batchCreateByBookId(bookId: BookId, authorIds: List<AuthorId>): Boolean {
        if (authorIds.isEmpty()) return false

        val records = authorIds.map { authorId ->
            dsl.insertInto(BOOK_AUTHOR_RELATIONS)
                .set(BOOK_AUTHOR_RELATIONS.BOOK_ID, bookId.value)
                .set(BOOK_AUTHOR_RELATIONS.AUTHOR_ID, authorId.value)
        }

        val result = dsl.batch(records).execute()
        return result.all { it > 0 }
    }
}