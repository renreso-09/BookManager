package com.github.renreso_09.bookmanager.domain.model

data class Book(
    val id: BookId?,
    val title: String,
    val price: Int,
    val status: BookStatus
)
