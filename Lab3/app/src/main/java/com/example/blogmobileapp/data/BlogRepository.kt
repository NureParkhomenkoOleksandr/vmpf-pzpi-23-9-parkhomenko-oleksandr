package com.example.blogmobileapp.data

import android.content.Context
import com.example.blogmobileapp.model.BlogArticle
import com.example.blogmobileapp.model.BlogComment
import org.json.JSONArray
import org.json.JSONObject

class BlogRepository(
    context: Context,
    private val defaultCategory: String,
    private val anonymousAuthor: String,
    private val allCategoriesLabel: String
) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun loadArticles(): MutableList<BlogArticle> {
        val articles = mutableListOf<BlogArticle>()
        val rawArticles = prefs.getString(KEY_ARTICLES, null) ?: return articles

        runCatching {
            val json = JSONArray(rawArticles)
            for (index in 0 until json.length()) {
                val item = json.optJSONObject(index) ?: continue
                val title = item.optString("title").trim()
                val body = item.optString("body").trim()
                if (title.isEmpty() || body.isEmpty()) {
                    continue
                }

                val createdAt = item.optLong("createdAt", System.currentTimeMillis())
                articles.add(
                    BlogArticle(
                        id = item.optLong("id", index.toLong() + 1),
                        title = title,
                        category = normalizeCategory(item.optString("category")),
                        body = body,
                        createdAt = createdAt,
                        updatedAt = item.optLong("updatedAt", createdAt),
                        comments = readComments(item.optJSONArray("comments"))
                    )
                )
            }
        }.onFailure {
            articles.clear()
        }

        return articles
    }

    fun saveArticles(articles: List<BlogArticle>) {
        val json = JSONArray()
        articles.forEach { article ->
            json.put(
                JSONObject()
                    .put("id", article.id)
                    .put("title", article.title)
                    .put("category", article.category)
                    .put("body", article.body)
                    .put("createdAt", article.createdAt)
                    .put("updatedAt", article.updatedAt)
                    .put("comments", commentsToJson(article.comments))
            )
        }
        prefs.edit().putString(KEY_ARTICLES, json.toString()).apply()
    }

    fun normalizeCategory(value: String): String {
        val category = value.trim()
        return if (category.isEmpty() || category.equals(allCategoriesLabel, ignoreCase = true)) {
            defaultCategory
        } else {
            category
        }
    }

    fun nextArticleId(articles: List<BlogArticle>): Long {
        return (articles.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    fun nextCommentId(article: BlogArticle): Long {
        return (article.comments.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    private fun commentsToJson(comments: List<BlogComment>): JSONArray {
        val json = JSONArray()
        comments.forEach { comment ->
            json.put(
                JSONObject()
                    .put("id", comment.id)
                    .put("author", comment.author)
                    .put("body", comment.body)
                    .put("createdAt", comment.createdAt)
            )
        }
        return json
    }

    private fun readComments(json: JSONArray?): MutableList<BlogComment> {
        val comments = mutableListOf<BlogComment>()
        if (json == null) {
            return comments
        }

        for (index in 0 until json.length()) {
            val item = json.optJSONObject(index) ?: continue
            val body = item.optString("body").trim()
            if (body.isEmpty()) {
                continue
            }

            comments.add(
                BlogComment(
                    id = item.optLong("id", index.toLong() + 1),
                    author = item.optString("author").trim().ifEmpty { anonymousAuthor },
                    body = body,
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return comments
    }

    private companion object {
        const val PREFS_NAME = "blog_storage"
        const val KEY_ARTICLES = "articles"
    }
}
