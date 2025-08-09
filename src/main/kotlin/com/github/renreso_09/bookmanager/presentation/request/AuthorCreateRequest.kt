package com.github.renreso_09.bookmanager.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class AuthorCreateRequest(
    @field:NotBlank(message = "著者の名前は必須です")
    val name: String,

    @field:NotBlank(message = "著者の生年月日は必須です")
    @field:Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}$",
        message = "生年月日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）"
    )
    val birthDate: String
)
