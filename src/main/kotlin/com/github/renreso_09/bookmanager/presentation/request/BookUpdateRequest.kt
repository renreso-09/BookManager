package com.github.renreso_09.bookmanager.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class BookUpdateRequest(
    @field:JsonProperty("title")
    @field:NotBlank(message = "書籍のタイトルは必須です")
    val title: String,

    @field:JsonProperty("price")
    @field:NotBlank(message = "書籍の価格は必須です")
    @field:Size(min = 0, message = "書籍の価格は0円以上に設定してください")
    val price: Int,

    @field:JsonProperty("status")
    @field:NotBlank(message = "書籍のステータスは必須です")
    @field:Pattern(
        regexp = "^(PREVIEW|PUBLISHED)$",
        message = "書籍のステータスは 'PREVIEW' または 'PUBLISHED' のいずれかでなければなりません"
    )
    val status: String,

    @field:JsonProperty("authors")
    @field:NotBlank(message = "書籍の著者は必須です")
    @field:NotEmpty(message = "著者は最低1人必要です")
    val authors: List<AuthorUpdateRequest>
)
