package com.github.renreso_09.bookmanager.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*

data class BookCreateRequest(
    @field:NotBlank(message = "書籍のタイトルは必須です")
    val title: String,

    @field:NotNull(message = "書籍の価格は必須です")
    @field:Min(value = 0, message = "書籍の価格は0以上でなければなりません")
    val price: Int,

    @field:NotBlank(message = "書籍のステータスは必須です")
    @field:Pattern(
        regexp = "^(PREVIEW|PUBLISHED)$",
        message = "書籍のステータスは 'PREVIEW' または 'PUBLISHED' のいずれかでなければなりません"
    )
    val status: String,

    @field:NotEmpty(message = "著者は最低1人必要です")
    val authors: List<AuthorCreateRequest>
)
