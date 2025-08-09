package com.github.renreso_09.bookmanager.query.dto

data class BookDTO(
    val id: Int,
    val title: String,
    val price: Int,
    val status: String,
    val authors: List<AuthorDTO>
)

data class AuthorDTO(
    val authorId: Int,
    val authorName: String,
    val authorBirthDate: String
)