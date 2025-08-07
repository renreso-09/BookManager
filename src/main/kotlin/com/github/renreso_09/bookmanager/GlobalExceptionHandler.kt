package com.github.renreso_09.bookmanager

import BadRequestException
import NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val message: String,
)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(ex: Exception): ErrorResponse {
        return ErrorResponse(
            message = ex.message ?: "予期せぬエラーが発生しました"
        )
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(ex: NotFoundException): ErrorResponse {
        return ErrorResponse(
            message = ex.message ?: "要素が見つかりませんでした"
        )
    }

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequestException(ex: BadRequestException): ErrorResponse {
        return ErrorResponse(
            message = ex.message ?: "不正なリクエストです"
        )
    }

    // バリデーションエラー用のハンドラーを追加
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: MethodArgumentNotValidException): ErrorResponse {
        val error = ex.bindingResult.fieldErrors.firstOrNull()
        return ErrorResponse(
            message = error?.defaultMessage ?: "不正なリクエストです"
        )
    }
}