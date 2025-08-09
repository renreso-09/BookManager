package com.github.renreso_09.bookmanager

import BadRequestException
import com.github.renreso_09.bookmanager.application.service.BookService
import com.github.renreso_09.bookmanager.application.service.BookServiceImpl
import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.domain.model.BookId
import com.github.renreso_09.bookmanager.domain.model.BookStatus
import com.github.renreso_09.bookmanager.presentation.request.AuthorCreateRequest
import com.github.renreso_09.bookmanager.presentation.request.AuthorUpdateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookUpdateRequest
import com.github.renreso_09.bookmanager.presentation.response.BookCreateResponse
import com.github.renreso_09.bookmanager.presentation.response.BookListResponse
import com.github.renreso_09.bookmanager.presentation.response.BookResponse
import com.github.renreso_09.bookmanager.query.BookListQuery
import com.github.renreso_09.bookmanager.query.dto.AuthorDTO
import com.github.renreso_09.bookmanager.query.dto.BookDTO
import com.github.renreso_09.bookmanager.repository.AuthorRepository
import com.github.renreso_09.bookmanager.repository.BookAuthorRelationRepository
import com.github.renreso_09.bookmanager.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class BookServiceImplTest {
    private lateinit var bookRepository: BookRepository
    private lateinit var authorRepository: AuthorRepository
    private lateinit var bookAuthorRelationRepository: BookAuthorRelationRepository
    private lateinit var bookListQuery: BookListQuery
    private lateinit var bookService: BookService

    @BeforeEach
    fun setUp() {
        // テスト用のリポジトリを初期化
        bookRepository = mockk()
        authorRepository = mockk()
        bookAuthorRelationRepository = mockk()
        bookListQuery = mockk()
        bookService = BookServiceImpl(
            bookRepository = bookRepository,
            authorRepository = authorRepository,
            bookAuthorRelationRepository = bookAuthorRelationRepository,
            bookListQuery = bookListQuery
        )
    }

    @Test
    fun `AuthorId=1に紐づく書籍を取得できる`() {
        val mockBookDTOs = listOf<BookDTO>(
            BookDTO(
                id = 1,
                title = "Book One",
                price = 1000,
                status = "PUBLISHED",
                authors = listOf(AuthorDTO(1, "山田太郎", "1990-01-01"))
            ),
            BookDTO(
                id = 2,
                title = "Book Two",
                price = 1500,
                status = "PUBLISHED",
                authors = listOf(
                    AuthorDTO(1, "山田太郎", "1990-01-01"),
                    AuthorDTO(2, "佐藤花子", "1995-05-20")
                )
            )
        )

        every { bookListQuery.findByAuthorId(AuthorId(1)) } returns mockBookDTOs

        val assertResponse = BookListResponse(
            books = mockBookDTOs.map { BookResponse.fromDTO(it) }
        )

        val response = bookService.findByAuthorId(1)
        assertEquals(2, response.books.size)
        assertEquals(assertResponse, response)
    }

    @Test
    fun `書籍の登録ができる`() {
        val authors = listOf<AuthorCreateRequest>(
            AuthorCreateRequest("山田太郎", "1990-01-01"),
            AuthorCreateRequest("佐藤花子", "1995-05-20")
        )
        val testRequest = BookCreateRequest(
            "New Book",
            1500,
            "PUBLISHED",
            authors
        )
        val mockBook = Book(
            id = null,
            title = "New Book",
            price = 1500,
            status = BookStatus.PUBLISHED
        )
        val mockAuthors = listOf(
            Author(null, "山田太郎", LocalDate.of(1990, 1,1)),
            Author(null, "佐藤花子", LocalDate.of(1995, 5, 20))
        )
        every { authorRepository.findByNames(listOf("山田太郎", "佐藤花子")) } returns emptyList()
        every { bookRepository.create(mockBook) } returns BookId(1)
        every { authorRepository.bulkCreate(mockAuthors) } returns listOf(AuthorId(1), AuthorId(2))
        every { bookAuthorRelationRepository.batchCreateByBookId(BookId(1), listOf(AuthorId(1), AuthorId(2))) } returns true

        val response: BookCreateResponse = bookService.create(testRequest)

        assertEquals("ok", response.message)
    }

    @Test
    fun `書籍の更新ができる`() {
        val authors = listOf<AuthorUpdateRequest>(
            AuthorUpdateRequest("山田太郎", "1990-01-01"),
            AuthorUpdateRequest("佐藤花子", "1995-05-20")
        )
        val testRequest = BookUpdateRequest(
            "Updated Book",
            2000,
            "PUBLISHED",
            authors
        )
        val mockBookId = BookId(1)
        val mockBook = Book(
            id = mockBookId,
            title = "Updated Book",
            price = 2000,
            status = BookStatus.PUBLISHED
        )
        val mockAuthors = listOf(
            Author(AuthorId(1), "山田太郎", LocalDate.of(1990, 1,1)),
            Author(null, "佐藤花子", LocalDate.of(1995, 5, 20))
        )
        every { bookRepository.findById(mockBookId) } returns mockBook
        every { bookRepository.update(mockBook) } returns mockBookId
        every { bookAuthorRelationRepository.deleteByBookId(mockBookId.value)} returns true
        every { authorRepository.findByNames(listOf("山田太郎", "佐藤花子")) } returns listOf(
            Author(AuthorId(1), "山田太郎", LocalDate.of(1990, 1,1))
        )
        every { authorRepository.bulkCreate(mockAuthors.filter { it.id == null }) } returns listOf(AuthorId(2))
        every { bookAuthorRelationRepository.batchCreateByBookId(mockBookId, listOf(AuthorId(1), AuthorId(2))) } returns true

        val response = bookService.update(1, testRequest)
        assertEquals("ok", response.message)
    }

    @Test
    fun `書籍の更新 - 書籍のステータスをPUBLISHEDからUNPUBLISHEDに変更できない`() {
        val authors = listOf<AuthorUpdateRequest>(
            AuthorUpdateRequest("山田太郎", "1990-01-01"),
            AuthorUpdateRequest("佐藤花子", "1995-05-20")
        )
        val testRequest = BookUpdateRequest(
            "Updated Book",
            2000,
            "UNPUBLISHED",
            authors
        )
        val mockBookId = BookId(1)
        val mockBook = Book(
            id = mockBookId,
            title = "Updated Book",
            price = 2000,
            status = BookStatus.PUBLISHED
        )
        every { bookRepository.findById(mockBookId) } returns mockBook

        val exception = assertThrows(BadRequestException::class.java) {
            bookService.update(1, testRequest)
        }

        assertEquals("PUBLISHEDからUNPUBLISHEDへの変更は許可されていません", exception.message)
    }

    @Test
    fun `書籍の更新 - 著者の生年月日が未来の日付を指定できない`() {
        val authors = listOf<AuthorUpdateRequest>(
            AuthorUpdateRequest("山田太郎", "1990-01-01"),
            AuthorUpdateRequest("佐藤花子", "2026-05-20")
        )
        val testRequest = BookUpdateRequest(
            "Updated Book",
            2000,
            "PUBLISHED",
            authors
        )
        val mockBookId = BookId(1)
        val mockBook = Book(
            id = mockBookId,
            title = "Updated Book",
            price = 2000,
            status = BookStatus.PUBLISHED
        )
        every { bookRepository.findById(mockBookId) } returns mockBook

        val exception = assertThrows(BadRequestException::class.java) {
            bookService.update(1, testRequest)
        }

        assertEquals("著者の生年月日は未来の日付にできません", exception.message)
    }
}