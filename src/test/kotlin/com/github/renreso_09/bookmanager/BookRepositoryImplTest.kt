package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.domain.model.BookId
import com.github.renreso_09.bookmanager.domain.model.BookStatus
import com.github.renreso_09.bookmanager.jooq.Tables.BOOKS
import com.github.renreso_09.bookmanager.repository.BookRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@JooqTest
@Transactional
@ActiveProfiles("test")
@Import(BookRepositoryImpl::class)
class BookRepositoryImplTest @Autowired constructor(
    private val bookRepository: BookRepositoryImpl,
    private val dsl: DSLContext
) {
    @BeforeEach
    fun setUp() {
        // スキーマ作成
        dsl.execute("CREATE SCHEMA IF NOT EXISTS book_manager")

        // スキーマ付きでテーブル削除・作成
        dsl.execute("DROP TABLE IF EXISTS book_manager.books")
        dsl.execute("DROP TABLE IF EXISTS book_manager.book_status")

        dsl.execute("""
            CREATE TABLE book_manager.book_status (
                status VARCHAR(20) PRIMARY KEY
            )
        """)

        dsl.execute("""
             INSERT INTO book_manager.book_status (status) VALUES 
             ('PUBLISHED'), ('UNPUBLISHED')
         """)

        dsl.execute("""
            CREATE TABLE book_manager.books (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                price INT NOT NULL,
                status VARCHAR(20) NOT NULL,
                FOREIGN KEY (status) REFERENCES book_manager.book_status (status)
            )
        """)
    }

    @Test
    fun `findByIdで登録した本が取得できる`() {
        // 事前にDBへ直接INSERT
        val insertedId = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, "Kotlin入門")
            .set(BOOKS.PRICE, 2000)
            .set(BOOKS.STATUS, "PUBLISHED")
            .returning(BOOKS.ID)
            .fetchOne()!!
            .getValue(BOOKS.ID)

        val book = bookRepository.findById(BookId(insertedId))

        assertThat(book).isNotNull
        assertThat(book!!.title).isEqualTo("Kotlin入門")
        assertThat(book.price).isEqualTo(2000)
        assertThat(book.status).isEqualTo(BookStatus.PUBLISHED)
    }

    @Test
    fun `createで本を登録できる`() {
        val newBook = Book(
            id = null,
            title = "Kotlin実践",
            price = 2500,
            status = BookStatus.UNPUBLISHED
        )

        val bookId = bookRepository.create(newBook)

        val stored = dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(bookId.value))
            .fetchOne()

        assertThat(stored).isNotNull
        assertThat(stored!!.getValue(BOOKS.TITLE)).isEqualTo("Kotlin実践")
    }

    @Test
    fun `updateで本の情報を更新できる`() {
        val insertedId = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, "旧タイトル")
            .set(BOOKS.PRICE, 1000)
            .set(BOOKS.STATUS, "UNPUBLISHED")
            .returning(BOOKS.ID)
            .fetchOne()!!
            .getValue(BOOKS.ID)

        val updatedBook = Book(
            id = BookId(insertedId),
            title = "新タイトル",
            price = 1500,
            status = BookStatus.PUBLISHED
        )

        bookRepository.update(updatedBook)

        val stored = dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(insertedId))
            .fetchOne()

        assertThat(stored!!.getValue(BOOKS.TITLE)).isEqualTo("新タイトル")
        assertThat(stored.getValue(BOOKS.PRICE)).isEqualTo(1500)
        assertThat(stored.getValue(BOOKS.STATUS)).isEqualTo("PUBLISHED")
    }

    @AfterEach
    fun tearDown() {
        dsl.execute("DROP ALL OBJECTS")
    }
}
