package com.github.renreso_09.bookmanager.presentation.response

import com.github.renreso_09.bookmanager.domain.model.Book

data class BookListResponse(
    val books: List<BookResponse>
)

data class BookResponse(
    val id: Int,
    val title: String,
    val price: Int,
    val status: String
) {
    companion object {
        fun fromDomain(book: Book): BookResponse {
            return BookResponse(
                id = book.id ?: throw IllegalArgumentException("Book ID cannot be null"),
                title = book.title,
                price = book.price,
                status = book.status
            )
        }
    }
}