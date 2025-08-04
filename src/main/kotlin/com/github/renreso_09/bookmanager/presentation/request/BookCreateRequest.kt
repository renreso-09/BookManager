package com.github.renreso_09.bookmanager.presentation.request

import com.fasterxml.jackson.annotation.JsonProperty

data class BookCreateRequest(
    @JsonProperty("title")
    val title: String,
    @JsonProperty("price")
    val price: Int,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("authors")
    val authors: List<AuthorCreateRequest>
)
