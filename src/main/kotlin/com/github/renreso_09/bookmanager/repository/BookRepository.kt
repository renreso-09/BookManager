package com.github.renreso_09.bookmanager.repository

import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.jooq.Tables.BOOKS
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

interface BookRepository {
    fun findAll(): List<Book>
    fun findById(id: Int): Book?
    fun findByAuthorId(authorId: Int): List<Book>
    fun create(book: Book): Book
    fun update(book: Book): Book
}

@Repository
class BookRepositoryImpl(private val dsl: DSLContext) : BookRepository {

    override fun findAll(): List<Book> {
        val bookRecords = dsl.selectFrom(BOOKS).fetch()
        return bookRecords.map { record ->
            Book(
                id = record.getValue(BOOKS.ID),
                title = record.getValue(BOOKS.TITLE),
                price = record.getValue(BOOKS.PRICE),
                status = record.getValue(BOOKS.STATUS)
            )
        }
    }

    override fun findById(id: Int): Book? {
        val record = dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne() ?: return null

        return Book(
            id = record.getValue(BOOKS.ID),
            title = record.getValue(BOOKS.TITLE),
            price = record.getValue(BOOKS.PRICE),
            status = record.getValue(BOOKS.STATUS)
        )
    }

//    AuthorIdに紐づく書籍を取得
    override fun findByAuthorId(authorId: Int): List<Book> {
        val bookRecords = dsl.select()
            .from(BOOKS)
            .innerJoin(BOOK_AUTHOR_RELATIONS).on(BOOKS.ID.eq(BOOK_AUTHOR_RELATIONS.BOOK_ID))
            .where(BOOK_AUTHOR_RELATIONS.AUTHOR_ID.eq(authorId))
            .fetch()
        return bookRecords.map { record ->
            Book(
                id = record.getValue(BOOKS.ID),
                title = record.getValue(BOOKS.TITLE),
                price = record.getValue(BOOKS.PRICE),
                status = record.getValue(BOOKS.STATUS)
            )
        }
    }

    // 新しい書籍を作成
    override fun create(book: Book): Book {
        val record = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS, book.status)
            .returning(BOOKS.ID)
            .fetchOne() ?: throw IllegalStateException("Failed to insert book")

        return book.copy(id = record.getValue(BOOKS.ID))
    }

    // 書籍を更新
    override fun update(book: Book): Book {
        dsl.update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS, book.status)
            .where(BOOKS.ID.eq(book.id))
            .execute()

        return book
    }
}