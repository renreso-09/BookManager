package com.github.renreso_09.bookmanager.application.service

import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import com.github.renreso_09.bookmanager.presentation.response.BookListResponse
import com.github.renreso_09.bookmanager.presentation.response.BookResponse
import com.github.renreso_09.bookmanager.repository.AuthorRepository
import com.github.renreso_09.bookmanager.repository.BookAuthorRelationRepository
import com.github.renreso_09.bookmanager.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface BookService {
    fun getAllBooks(): BookListResponse
    fun findByAuthorId(authorId: Int): BookListResponse
    fun create(request: BookCreateRequest)
}

@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookAuthorRelationRepository: BookAuthorRelationRepository
) : BookService {

    @Transactional(readOnly = true)
    override fun getAllBooks(): BookListResponse {
        val books = bookRepository.findAll().map { book ->
            BookResponse.fromDomain(book)
        }
        return BookListResponse(books = books)
    }

    @Transactional(readOnly = true)
    override fun findByAuthorId(authorId: Int): BookListResponse {
        val books = bookRepository.findByAuthorId(authorId).map { book ->
            BookResponse.fromDomain(book)
        }
        return BookListResponse(books = books)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun create(request: BookCreateRequest) {
        // 著者が1人以上いることを確認
        if (request.authors.isEmpty()) {
            throw IllegalArgumentException("At least one author is required")
        }
        // 書籍の登録
        val book = Book(
            id = null,
            title = request.title,
            price = request.price,
            status = request.status
        )
        val createdBook = bookRepository.create(book)
        // 著者の登録
        request.authors.map { authorRequest ->
            // nameで著者を検索し、存在しない場合は新規作成
           val author = authorRepository.findByName(authorRequest.name) ?:
            Author(
                id = null,
                name = authorRequest.name,
                birthDay = authorRequest.birthDay
            )
            val authorId = author.id ?: authorRepository.create(author).id
            // 書籍と著者の関連を登録
            bookAuthorRelationRepository.create(createdBook.id!!, authorId!!)
        }
    }
}