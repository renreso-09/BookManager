package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.presentation.request.AuthorUpdateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookUpdateRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BookUpdateRequestValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `必須項目が全て正しい場合はバリデーションエラーにならない`() {
        val request = BookUpdateRequest(
            title = "Kotlin入門",
            price = 2000,
            status = "PUBLISHED",
            authors = listOf(AuthorUpdateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `タイトルが空文字だとエラーになる`() {
        val request = BookUpdateRequest(
            title = "",
            price = 2000,
            status = "PUBLISHED",
            authors = listOf(AuthorUpdateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍のタイトルは必須です")
        }
    }

    @Test
    fun `価格がマイナスだとエラーになる`() {
        val request = BookUpdateRequest(
            title = "Kotlin入門",
            price = -1,
            status = "PUBLISHED",
            authors = listOf(AuthorUpdateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍の価格は0以上でなければなりません")
        }
    }

    @Test
    fun `ステータスが不正だとエラーになる`() {
        val request = BookUpdateRequest(
            title = "Kotlin入門",
            price = 2000,
            status = "INVALID_STATUS",
            authors = listOf(AuthorUpdateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍のステータスは 'UNPUBLISHED' または 'PUBLISHED' のいずれかでなければなりません")
        }
    }

    @Test
    fun `著者が空リストだとエラーになる`() {
        val request = BookUpdateRequest(
            title = "Kotlin入門",
            price = 2000,
            status = "PUBLISHED",
            authors = emptyList()
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("著者は最低1人必要です")
        }
    }
}
