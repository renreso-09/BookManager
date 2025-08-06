package com.github.renreso_09.bookmanager.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class AuthorUpdateRequest(
    @field:JsonProperty("name")
    @field:NotBlank(message = "著者の名前は必須です")
    val name: String,

    @field:JsonProperty("birthDay")
    @field:NotBlank(message = "著者の誕生日は必須です")
    val birthDay: LocalDate
)
