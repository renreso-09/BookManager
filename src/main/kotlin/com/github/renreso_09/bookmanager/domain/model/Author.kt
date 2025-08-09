package com.github.renreso_09.bookmanager.domain.model

import java.time.LocalDate

data class Author(
    val id: AuthorId?,
    val name: String,
    val birthDate: LocalDate
)
