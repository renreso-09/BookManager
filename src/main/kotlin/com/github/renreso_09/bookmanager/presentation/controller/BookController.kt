package com.github.renreso_09.bookmanager.presentation.controller

import com.github.renreso_09.bookmanager.application.service.BookService
import com.github.renreso_09.bookmanager.presentation.request.BookCreateRequest
import com.github.renreso_09.bookmanager.presentation.request.BookUpdateRequest
import com.github.renreso_09.bookmanager.presentation.response.BookListResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {
    @GetMapping("/authors/{authorId}")
    fun findByAuthorId(@PathVariable authorId: Int): BookListResponse {
        return bookService.findByAuthorId(authorId)
    }

    @PostMapping
    fun create(@RequestBody @Valid request: BookCreateRequest) {
        bookService.create(request)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody @Valid request: BookUpdateRequest) {
        bookService.update(id, request)
    }
}