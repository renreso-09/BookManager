package com.github.renreso_09.bookmanager.presentation.controller

import com.github.renreso_09.bookmanager.application.service.BookService
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import com.github.renreso_09.bookmanager.presentation.response.BookListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {
    @GetMapping
    fun getAll(): BookListResponse {
        return bookService.getAllBooks()
    }

    @GetMapping("/authors/{authorId}")
    fun findByAuthorId(@PathVariable authorId: Int): BookListResponse {
        return bookService.findByAuthorId(authorId)
    }

    @PostMapping
    fun create(@RequestBody request: BookCreateRequest): ResponseEntity<Void> {
        try {
            println(request)
            bookService.create(request)
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }
}