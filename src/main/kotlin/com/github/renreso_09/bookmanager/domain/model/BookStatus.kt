package com.github.renreso_09.bookmanager.domain.model

import BadRequestException

enum class BookStatus(val value: String) {
    PUBLISHED("PUBLISHED"),
    UNPUBLISHED("UNPUBLISHED");

    companion object {
        fun fromString(status: String): BookStatus {
            return BookStatus.entries.find { it.value == status }
                ?: throw BadRequestException("無効なステータスです")
        }

        fun updateStatus(currentStatus: BookStatus, newStatusString: String): BookStatus {
            //  PUBLISHEDからUNPUBLISHEDへの変更は許可しない
            val newStatus = fromString(newStatusString)
            if (currentStatus == PUBLISHED && newStatus == UNPUBLISHED) {
                throw BadRequestException("PUBLISHEDからUNPUBLISHEDへの変更は許可されていません")
            }
            return newStatus
        }
    }
}