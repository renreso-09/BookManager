package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOKS
import com.github.renreso_09.bookmanager.query.BookListQueryImpl
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@JooqTest
@Import(BookListQueryImpl::class)
class BookListQueryImplTest @Autowired constructor(
    private val dsl: DSLContext,
    private val bookListQuery: BookListQueryImpl,
) {

    @BeforeEach
    fun setUp() {
        dsl.deleteFrom(BOOK_AUTHOR_RELATIONS).execute()
        dsl.deleteFrom(BOOKS).execute()
        dsl.deleteFrom(AUTHORS).execute()
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
}
