package com.github.renreso_09.bookmanager.application.service

import BadRequestException
import NotFoundException
import com.github.renreso_09.bookmanager.domain.model.Author
import com.github.renreso_09.bookmanager.domain.model.AuthorId
import com.github.renreso_09.bookmanager.domain.model.Book
import com.github.renreso_09.bookmanager.domain.model.BookId
import com.github.renreso_09.bookmanager.domain.model.BookStatus
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookUpdateRequest
import com.github.renreso_09.bookmanager.presentation.response.BookCreateResponse
import com.github.renreso_09.bookmanager.presentation.response.BookListResponse
import com.github.renreso_09.bookmanager.presentation.response.BookResponse
import com.github.renreso_09.bookmanager.presentation.response.BookUpdateResponse
import com.github.renreso_09.bookmanager.repository.AuthorRepository
import com.github.renreso_09.bookmanager.repository.BookAuthorRelationRepository
import com.github.renreso_09.bookmanager.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface BookService {
    fun findByAuthorId(authorId: Int): BookListResponse
    fun create(request: BookCreateRequest): BookCreateResponse
    fun update(bookId: Int, request: BookUpdateRequest): BookUpdateResponse
}

@Service
class BookServiceImpl(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
    private val bookAuthorRelationRepository: BookAuthorRelationRepository
) : BookService {
    @Transactional(readOnly = true)
    override fun findByAuthorId(authorId: Int): BookListResponse {
        val books = bookRepository.findByAuthorId(AuthorId(authorId)).map { book ->
            BookResponse.fromDomain(book)
        }
        return BookListResponse(books = books)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun create(request: BookCreateRequest): BookCreateResponse {
        // 著者が1人以上いることを確認
        if (request.authors.isEmpty()) {
            throw BadRequestException("著者は最低1人必要です")
        }
        // 著者の誕生日が現在の日付より未来でないことを確認
        request.authors.forEach { authorRequest ->
            val parseBirthDay = try {
                LocalDate.parse(authorRequest.birthDay)
            } catch (e: Exception) {
                throw BadRequestException("誕生日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）")
            }
            if (parseBirthDay.isAfter(LocalDate.now())) {
                throw BadRequestException("著者の誕生日は未来の日付にできません")
            }
        }
        val newBookStatus = BookStatus.fromString(request.status)
        // 書籍の登録
        val book = Book(
            id = null,
            title = request.title,
            price = request.price,
            status = newBookStatus
        )
        val createdBookId = bookRepository.create(book)
        // 著者を検索
        val existAuthors = authorRepository.findByNames(request.authors.map { it.name })
        // 新規著者のリストを作成
        val newAuthors = request.authors.filter { authorRequest ->
            existAuthors.none { it.name == authorRequest.name }
        }.map { authorRequest ->
            // 誕生日をLocalDateに変換
            val birthDay = try {
                LocalDate.parse(authorRequest.birthDay)
            } catch (e: Exception) {
                throw BadRequestException("誕生日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）")
            }
            Author(
                id = null,
                name = authorRequest.name,
                birthDay = birthDay
            )
        }
        // 新規著者を登録
        val createdAuthorIds = authorRepository.bulkCreate(newAuthors)
        // 書籍と著者の関連を登録
        val authorIds = (existAuthors.map { it.id } + createdAuthorIds).mapNotNull { it }
        if (authorIds.isEmpty()) {
            throw BadRequestException("著者が見つかりませんでした")
        }
        bookAuthorRelationRepository.batchCreateByBookId(createdBookId, authorIds)
        return BookCreateResponse(message = "ok")
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun update(bookId: Int, request: BookUpdateRequest): BookUpdateResponse {
        val book = bookRepository.findById(BookId(bookId))
            ?: throw NotFoundException("書籍が見つかりませんでした")
        // 著者の誕生日が現在の日付より未来でないことを確認
        request.authors.forEach { authorRequest ->
            val parseBirthDay = try {
                LocalDate.parse(authorRequest.birthDay)
            } catch (e: Exception) {
                throw BadRequestException("誕生日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）")
            }
            if (parseBirthDay.isAfter(LocalDate.now())) {
                throw BadRequestException("著者の誕生日は未来の日付にできません")
            }
        }
        val newBookStatus = BookStatus.updateStatus(
            currentStatus = book.status,
            newStatusString = request.status
        )
        // 書籍の更新
        val updatedBook = book.copy(
            title = request.title,
            price = request.price,
            status = newBookStatus
        )
        bookRepository.update(updatedBook)
        // 既存の著者との関連を削除
        bookAuthorRelationRepository.deleteByBookId(bookId)
        // 著者を検索
        val existAuthors = authorRepository.findByNames(request.authors.map { it.name })
        // 新規著者のリストを作成
        val newAuthors = request.authors.filter { authorRequest ->
            existAuthors.none { it.name == authorRequest.name }
        }.map { authorRequest ->
            val birthDay = try {
                LocalDate.parse(authorRequest.birthDay)
            } catch (e: Exception) {
                throw BadRequestException("誕生日はyyyy-MM-dd形式で入力してください（例: 2024-01-15）")
            }
            Author(
                id = null,
                name = authorRequest.name,
                birthDay = birthDay
            )
        }
        // 新規著者を登録
        val createdAuthorIds = authorRepository.bulkCreate(newAuthors)
        // 書籍と著者の関連を登録
        val authorIds = (existAuthors.map { it.id } + createdAuthorIds).mapNotNull { it }
        if (authorIds.isEmpty()) {
            throw BadRequestException("著者が見つかりませんでした")
        }
        bookAuthorRelationRepository.batchCreateByBookId(BookId(bookId), authorIds)
        return BookUpdateResponse("ok")
    }
}