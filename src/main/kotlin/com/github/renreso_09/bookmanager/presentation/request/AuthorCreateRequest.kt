package com.github.renreso_09.bookmanager.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class AuthorCreateRequest(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("birthDay")
    val birthDay: LocalDate
)
