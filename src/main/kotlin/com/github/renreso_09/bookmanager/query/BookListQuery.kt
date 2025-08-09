package com.github.renreso_09.bookmanager.query

import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS
import com.github.renreso_09.bookmanager.jooq.tables.Books.BOOKS
import com.github.renreso_09.bookmanager.query.dto.AuthorDTO
import com.github.renreso_09.bookmanager.query.dto.BookDTO
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

interface BookListQuery {
    fun findByAuthorId(authorId: AuthorId): List<BookDTO>
}

@Repository
class BookListQueryImpl(private val dsl: DSLContext): BookListQuery {
    override fun findByAuthorId(authorId: AuthorId): List<BookDTO> {
        val result = dsl
            .select(
                BOOKS.ID,
                BOOKS.TITLE,
                BOOKS.PRICE,
                BOOKS.STATUS,
                AUTHORS.ID,
                AUTHORS.NAME,
                AUTHORS.BIRTH_DATE
            )
            .from(BOOKS)
            .join(BOOK_AUTHOR_RELATIONS).on(BOOKS.ID.eq(BOOK_AUTHOR_RELATIONS.BOOK_ID))
            .join(AUTHORS).on(BOOK_AUTHOR_RELATIONS.AUTHOR_ID.eq(AUTHORS.ID))
            .where(BOOKS.ID.`in`(
                dsl.select(BOOK_AUTHOR_RELATIONS.BOOK_ID)
                    .from(BOOK_AUTHOR_RELATIONS)
                    .where(BOOK_AUTHOR_RELATIONS.AUTHOR_ID.eq(authorId.value))
            ))
            .fetch()
        val groupByResult = result.groupBy { it.getValue(BOOKS.ID) }
       val items = groupByResult.map { (bookId, records) ->
           val authors: List<AuthorDTO> = records.map { record ->
               AuthorDTO(
                   authorId = record.getValue(AUTHORS.ID),
                   authorName = record.getValue(AUTHORS.NAME),
                   authorBirthDate = record.getValue(AUTHORS.BIRTH_DATE).toString()
               )
           }
           BookDTO(
               id = bookId,
               title = records.first().getValue(BOOKS.TITLE),
               price = records.first().getValue(BOOKS.PRICE),
               status = records.first().getValue(BOOKS.STATUS),
               authors = authors
           )
       }
        return items
    }

}