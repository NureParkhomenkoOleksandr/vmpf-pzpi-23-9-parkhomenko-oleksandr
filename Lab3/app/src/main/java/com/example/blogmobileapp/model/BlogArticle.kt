package com.example.blogmobileapp.model

data class BlogArticle(
    val id: Long,
    var title: String,
    var category: String,
    var body: String,
    val createdAt: Long,
    var updatedAt: Long,
    val comments: MutableList<BlogComment> = mutableListOf()
)
