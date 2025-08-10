package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.repository.AuthorRepositoryImpl
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
@Import(AuthorRepositoryImpl::class)
class AuthorRepositoryImplTest @Autowired constructor(
    private val authorRepository: AuthorRepositoryImpl,
    private val dsl: DSLContext,
) {
    @BeforeEach
    fun setUp() {
        // スキーマ作成
        dsl.execute("CREATE SCHEMA IF NOT EXISTS book_manager")
        // スキーマ付きでテーブル削除・作成
        dsl.execute("DROP TABLE IF EXISTS book_manager.authors")

        dsl.execute("""
            CREATE TABLE book_manager.authors (
              id INT AUTO_INCREMENT PRIMARY KEY,
              name TEXT NOT NULL,
              birth_date DATE NOT NULL
            );
        """)
    }

    @Test
    fun `bulkCreate は複数著者をまとめて登録しIDを返す`() {
        val authors = listOf(
            Author(id = null, name = "著者A", birthDate = LocalDate.of(1990, 1, 1)),
            Author(id = null, name = "著者B", birthDate = LocalDate.of(1991, 2, 2)),
        )

        val ids = authorRepository.bulkCreate(authors)

        assertThat(ids).hasSize(2)
        assertThat(ids.map { it.value }).allMatch { it > 0 }
    }

    @Test
    fun `findByNames は指定した名前の著者を返す`() {
        dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "田中太郎")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1985, 3, 15))
            .execute()

        dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "鈴木次郎")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1990, 4, 10))
            .execute()

        val results = authorRepository.findByNames(listOf("鈴木次郎"))

        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("鈴木次郎")
    }

    @Test
    fun `bulkUpdateは複数著者の情報を更新する`() {
        // 初期データを挿入
        val author1 = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "佐藤花子")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1988, 5, 20))
            .returning(AUTHORS.ID)
            .fetchOne()!!

        val author2 = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, "山田一郎")
            .set(AUTHORS.BIRTH_DATE, LocalDate.of(1992, 6, 25))
            .returning(AUTHORS.ID)
            .fetchOne()!!

        // 更新する著者リスト
        val authorsToUpdate = listOf(
            Author(id = AuthorId(author1.id), name = "佐藤花子", birthDate = LocalDate.of(1998, 5, 20)),
            Author(id = AuthorId(author2.id), name = "山田一郎", birthDate = LocalDate.of(1992, 8, 25)),
        )

        // 更新実行
        authorRepository.bulkUpdate(authorsToUpdate)

        // 更新後のデータを確認
        val updatedAuthors = dsl.selectFrom(AUTHORS).fetch()

        assertThat(updatedAuthors).hasSize(2)
        assertThat(updatedAuthors[0].birthDate).isEqualTo(LocalDate.of(1998, 5, 20))
        assertThat(updatedAuthors[1].birthDate).isEqualTo(LocalDate.of(1992, 8, 25))
    }

    @AfterEach
    fun tearDown() {
        dsl.execute("DROP ALL OBJECTS")
    }
}
