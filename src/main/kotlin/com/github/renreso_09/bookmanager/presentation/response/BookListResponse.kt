package com.github.renreso_09.bookmanager.presentation.response

import com.github.renreso_09.bookmanager.query.dto.BookDTO

data class BookListResponse(
    val books: List<BookResponse>
)

data class BookResponse(
    val id: Int,
    val title: String,
    val price: Int,
    val status: String,
    val authors: List<AuthorResponse>
) {
    companion object {
        fun fromDTO(dto: BookDTO): BookResponse {
            return BookResponse(
                id = dto.id,
                title = dto.title,
                price = dto.price,
                status = dto.status,
                authors = dto.authors.map { author ->
                    AuthorResponse(
                        id = author.authorId,
                        name = author.authorName,
                        birthDate = author.authorBirthDate
                    )
                }
            )
        }
    }
}