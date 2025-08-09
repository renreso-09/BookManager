package com.github.renreso_09.bookmanager

import com.github.renreso_09.bookmanager.presentation.request.AuthorUpdateRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorUpdateRequestValidationTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `全て正しい場合はエラーなし`() {
        val request = AuthorUpdateRequest(
            name = "山田太郎",
            birthDate = "1990-05-21"
        )

        val violations = validator.validate(request)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `名前が空文字だとエラー`() {
        val request = AuthorUpdateRequest(
            name = "",
            birthDate = "1990-05-21"
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("著者の名前は必須です")
        }
    }

    @Test
    fun `生年月日が空文字だとエラー`() {
        val request = AuthorUpdateRequest(
            name = "山田太郎",
            birthDate = ""
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message).isEqualTo("著者の生年月日は必須です")
        }
    }

    @Test
    fun `生年月日の形式が不正だとエラー`() {
        val request = AuthorUpdateRequest(
            name = "山田太郎",
            birthDate = "1990年05月21"
        )

        val violations = validator.validate(request)
        assertThat(violations).anySatisfy {
            assertThat(it.message)
                .isEqualTo("生年月日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）")
        }
    }
}
