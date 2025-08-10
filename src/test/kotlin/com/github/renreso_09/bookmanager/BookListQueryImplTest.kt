package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.jooq.Tables.*
import com.github.renreso_09.bookmanager.query.BookListQueryImpl
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
import java.time.LocalDate

@JooqTest
@Transactional
@ActiveProfiles("test")
@Import(BookListQueryImpl::class)
class BookListQueryImplTest @Autowired constructor(
    private val dsl: DSLContext,
    private val bookListQuery: BookListQueryImpl,
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

        dsl.execute("""
            CREATE TABLE book_manager.authors (
              id INT AUTO_INCREMENT PRIMARY KEY,
              name TEXT NOT NULL,
              birth_date DATE NOT NULL
            );
        """)

        dsl.execute("""
            CREATE TABLE book_manager.book_author_relations (
                book_id BIGINT NOT NULL,
                author_id INT NOT NULL,
                PRIMARY KEY (book_id, author_id),
                FOREIGN KEY (book_id) REFERENCES book_manager.books (id),
                FOREIGN KEY (author_id) REFERENCES book_manager.authors (id)
            )
        """)
    }

    @Test
    fun `findByAuthorId は指定著者IDに紐づく書籍と著者情報を返す`() {
        // 著者登録
        val authorId1 = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "山田太郎")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1980, 5, 20))
            .returning(AUTHORS.ID)
            .fetchOne()!!.getValue(AUTHORS.ID)

        val authorId2 = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "鈴木花子")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1990, 3, 15))
            .returning(AUTHORS.ID)
            .fetchOne()!!.getValue(AUTHORS.ID)

        // 書籍登録
        val bookId1 = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, "Kotlin入門")
            .set(BOOKS.PRICE, 2500)
            .set(BOOKS.STATUS, "PUBLISHED")
            .returning(BOOKS.ID)
            .fetchOne()!!.getValue(BOOKS.ID)

        val bookId2 = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, "Spring Boot実践")
            .set(BOOKS.PRICE, 3000)
            .set(BOOKS.STATUS, "UNPUBLISHED")
            .returning(BOOKS.ID)
            .fetchOne()!!.getValue(BOOKS.ID)

        // 書籍と著者の紐付け登録
        dsl.insertInto(BOOK_AUTHOR_RELATIONS)
            .set(BOOK_AUTHOR_RELATIONS.BOOK_ID, bookId1)
            .set(BOOK_AUTHOR_RELATIONS.AUTHOR_ID, authorId1)
            .execute()

        dsl.insertInto(BOOK_AUTHOR_RELATIONS)
            .set(BOOK_AUTHOR_RELATIONS.BOOK_ID, bookId1)
            .set(BOOK_AUTHOR_RELATIONS.AUTHOR_ID, authorId2)
            .execute()

        dsl.insertInto(BOOK_AUTHOR_RELATIONS)
            .set(BOOK_AUTHOR_RELATIONS.BOOK_ID, bookId2)
            .set(BOOK_AUTHOR_RELATIONS.AUTHOR_ID, authorId2)
            .execute()

        // テスト対象メソッド実行
        val results = bookListQuery.findByAuthorId(AuthorId(authorId2))

        // 結果検証
        assertThat(results).hasSize(2)
        val kotlinBook = results.find { it.title == "Kotlin入門" }!!
        assertThat(kotlinBook.price).isEqualTo(2500)
        assertThat(kotlinBook.status).isEqualTo("PUBLISHED")
        assertThat(kotlinBook.authors.map { it.authorName }).containsExactlyInAnyOrder("山田太郎", "鈴木花子")

        val springBook = results.find { it.title == "Spring Boot実践" }!!
        assertThat(springBook.price).isEqualTo(3000)
        assertThat(springBook.status).isEqualTo("UNPUBLISHED")
        assertThat(springBook.authors.map { it.authorName }).containsExactly("鈴木花子")
    }

    @AfterEach
    fun tearDown() {
        dsl.execute("DROP ALL OBJECTS")
    }
}
