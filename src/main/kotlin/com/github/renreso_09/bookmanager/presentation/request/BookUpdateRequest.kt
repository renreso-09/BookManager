package com.github.renreso_09.bookmanager.presentation.request

import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class BookUpdateRequest(
    @field:NotBlank(message = "書籍のタイトルは必須です")
    val title: String,

    @field:NotNull(message = "書籍の価格は必須です")
    @field:Min(value = 0, message = "書籍の価格は0以上でなければなりません")
    val price: Int,

    @field:NotBlank(message = "書籍のステータスは必須です")
    @field:Pattern(
        regexp = "^(UNPUBLISHED|PUBLISHED)$",
        message = "書籍のステータスは 'UNPUBLISHED' または 'PUBLISHED' のいずれかでなければなりません"
    )
    val status: String,

    @field:NotEmpty(message = "著者は最低1人必要です")
    @field:Valid
    val authors: List<AuthorUpdateRequest>
)
