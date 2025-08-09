package com.github.renreso_09.bookmanager.repository

import InternalException
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.domain.model.BookId
import com.github.renreso_09.bookmanager.domain.model.BookStatus
import com.github.renreso_09.bookmanager.jooq.Tables.BOOKS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

interface BookRepository {
    fun findById(id: BookId): Book?
    fun findByAuthorId(authorId: AuthorId): List<Book>
    fun create(book: Book): BookId
    fun update(book: Book): BookId
}

@Repository
class BookRepositoryImpl(private val dsl: DSLContext) : BookRepository {
    override fun findById(id: BookId): Book? {
        val record = dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id.value))
            .fetchOne() ?: return null

        return Book(
            id = BookId(record.getValue(BOOKS.ID)),
            title = record.getValue(BOOKS.TITLE),
            price = record.getValue(BOOKS.PRICE),
            status = BookStatus.fromString(record.getValue(BOOKS.STATUS))
        )
    }

    // AuthorIdに紐づく書籍を取得
    override fun findByAuthorId(authorId: AuthorId): List<Book> {
        val bookRecords = dsl.select()
            .from(BOOKS)
            .innerJoin(BOOK_AUTHOR_RELATIONS).on(BOOKS.ID.eq(BOOK_AUTHOR_RELATIONS.BOOK_ID))
            .where(BOOK_AUTHOR_RELATIONS.AUTHOR_ID.eq(authorId.value))
            .fetch()
        return bookRecords.map { record ->
            Book(
                id = BookId(record.getValue(BOOKS.ID)),
                title = record.getValue(BOOKS.TITLE),
                price = record.getValue(BOOKS.PRICE),
                status = BookStatus.fromString(record.getValue(BOOKS.STATUS))
            )
        }
    }

    // 新しい書籍を作成
    override fun create(book: Book): BookId {
        val record = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS, book.status.value)
            .returning(BOOKS.ID)
            .fetchOne() ?: throw InternalException("書籍の作成に失敗しました")

        return BookId(record.getValue(BOOKS.ID))
    }

    // 書籍を更新
    override fun update(book: Book): BookId {
        if (book.id == null) {
            throw InternalException("書籍IDがありません")
        }
        val record = dsl.update(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.STATUS, book.status.value)
            .where(BOOKS.ID.eq(book.id.value))
            .execute()

        return BookId(record)
    }
}