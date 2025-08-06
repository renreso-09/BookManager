package com.github.renreso_09.bookmanager.domain.model

enum class BookStatus(val value: String) {
    PREVIEW("PREVIEW"),
    PUBLISHED("PUBLISHED");

    companion object {
        fun fromString(status: String): BookStatus {
            return BookStatus.entries.find { it.value == status }
                ?: throw IllegalArgumentException("Invalid book status: $status")
        }
    }
}