package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.jooq.Tables.AUTHORS
import com.github.renreso_09.bookmanager.jooq.Tables.BOOK_AUTHOR_RELATIONS
import com.github.renreso_09.bookmanager.repository.AuthorRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@JooqTest
@Import(AuthorRepositoryImpl::class)
class AuthorRepositoryImplTest @Autowired constructor(
    private val authorRepository: AuthorRepositoryImpl,
    private val dsl: DSLContext,
) {
    @BeforeEach
    fun setUp() {
        dsl.deleteFrom(BOOK_AUTHOR_RELATIONS).execute()
        dsl.deleteFrom(AUTHORS).execute()
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
}
