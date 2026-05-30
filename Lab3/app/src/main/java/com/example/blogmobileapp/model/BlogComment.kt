package com.example.blogmobileapp.model

data class BlogComment(
    val id: Long,
    val author: String,
    val body: String,
    val createdAt: Long
)
