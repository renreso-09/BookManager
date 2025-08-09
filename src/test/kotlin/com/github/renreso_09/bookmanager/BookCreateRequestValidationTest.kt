package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.presentation.request.AuthorCreateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BookCreateRequestValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `必須項目が全て正しい場合はバリデーションエラーにならない`() {
        val request = BookCreateRequest(
            title = "Kotlin入門",
            price = 2000,
            status = "PUBLISHED",
            authors = listOf(AuthorCreateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `タイトルが空文字だとエラーになる`() {
        val request = BookCreateRequest(
            title = "",
            price = 2000,
            status = "PUBLISHED",
            authors = listOf(AuthorCreateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍のタイトルは必須です")
        }
    }

    @Test
    fun `価格がマイナスだとエラーになる`() {
        val request = BookCreateRequest(
            title = "Kotlin入門",
            price = -1,
            status = "PUBLISHED",
            authors = listOf(AuthorCreateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍の価格は0以上でなければなりません")
        }
    }

    @Test
    fun `ステータスが不正だとエラーになる`() {
        val request = BookCreateRequest(
            title = "Kotlin入門",
            price = 2000,
            status = "INVALID_STATUS",
            authors = listOf(AuthorCreateRequest(name = "山田太郎", birthDate = "1999-01-01"))
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("書籍のステータスは 'UNPUBLISHED' または 'PUBLISHED' のいずれかでなければなりません")
        }
    }

    @Test
    fun `著者が空リストだとエラーになる`() {
        val request = BookCreateRequest(
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
