package com.github.renreso_09.bookmanager.domain.model

import BadRequestException

enum class BookStatus(val value: String) {
    PREVIEW("PREVIEW"),
    PUBLISHED("PUBLISHED");

    companion object {
        fun fromString(status: String): BookStatus {
            return BookStatus.entries.find { it.value == status }
                ?: throw BadRequestException("無効なステータスです")
        }

        fun updateStatus(currentStatus: BookStatus, newStatusString: String): BookStatus {
            //  PUBLISHEDからPREVIEWへの変更は許可しない
            val newStatus = fromString(newStatusString)
            if (currentStatus == PUBLISHED && newStatus == PREVIEW) {
                throw BadRequestException("PUBLISHEDからPREVIEWへの変更は許可されていません")
            }
            // ステータスを更新
            return newStatus
        }
    }
}