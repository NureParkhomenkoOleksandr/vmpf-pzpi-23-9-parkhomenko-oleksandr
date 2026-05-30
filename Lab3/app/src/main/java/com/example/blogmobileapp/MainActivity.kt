package com.example.blogmobileapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.blogmobileapp.data.BlogRepository
import com.example.blogmobileapp.model.BlogArticle
import com.example.blogmobileapp.model.BlogComment
import com.example.blogmobileapp.ui.BlogUiFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var postsContainer: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var addArticleFab: ExtendedFloatingActionButton
    private lateinit var repository: BlogRepository
    private lateinit var uiFactory: BlogUiFactory
    private lateinit var activeCategory: String

    private val articles = mutableListOf<BlogArticle>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        postsContainer = findViewById(R.id.postsContainer)
        emptyStateText = findViewById(R.id.emptyStateText)
        addArticleFab = findViewById(R.id.addArticleFab)
        activeCategory = allCategoriesLabel()
        repository = BlogRepository(
            context = this,
            defaultCategory = getString(R.string.default_category),
            anonymousAuthor = getString(R.string.anonymous),
            allCategoriesLabel = allCategoriesLabel()
        )
        uiFactory = BlogUiFactory(this, dateFormat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addArticleFab.setOnClickListener { showArticleDialog() }
        articles.addAll(repository.loadArticles())
        renderBlog()
    }

    private fun renderBlog() {
        renderCategories()
        renderArticles()
    }

    private fun renderCategories() {
        val categories = availableCategories()
        if (activeCategory !in categories) {
            activeCategory = allCategoriesLabel()
        }

        categoryChipGroup.removeAllViews()
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = category == activeCategory
                setTextColor(color(R.color.category_text))
                chipBackgroundColor = tint(
                    if (category == activeCategory) {
                        R.color.category_background
                    } else {
                        R.color.surface_card
                    }
                )
                setOnClickListener {
                    activeCategory = category
                    renderBlog()
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun renderArticles() {
        postsContainer.removeAllViews()

        val visibleArticles = articles
            .filter { activeCategory == allCategoriesLabel() || it.category == activeCategory }
            .sortedByDescending { it.updatedAt }

        emptyStateText.text = if (articles.isEmpty()) {
            getString(R.string.empty_blog)
        } else {
            getString(R.string.empty_category, activeCategory)
        }
        emptyStateText.isVisible = visibleArticles.isEmpty()

        visibleArticles.forEach { article ->
            postsContainer.addView(
                uiFactory.createArticleCard(
                    article = article,
                    onComment = ::showCommentDialog,
                    onEdit = ::showArticleDialog,
                    onDelete = ::confirmDelete
                )
            )
        }
    }

    private fun showArticleDialog(article: BlogArticle? = null) {
        val titleInput = createDialogInput(getString(R.string.article_title_hint))
        val categoryInput = createDialogInput(getString(R.string.article_category_hint))
        val bodyInput = createDialogInput(getString(R.string.article_content_hint), multiline = true)

        titleInput.setText(article?.title.orEmpty())
        categoryInput.setText(article?.category.orEmpty())
        bodyInput.setText(article?.body.orEmpty())

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(titleInput)
            addView(categoryInput)
            addView(bodyInput)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (article == null) {
                    R.string.dialog_add_article
                } else {
                    R.string.dialog_edit_article
                }
            )
            .setView(form)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(
                if (article == null) {
                    R.string.add
                } else {
                    R.string.save
                },
                null
            )
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val title = titleInput.text.toString().trim()
                val category = repository.normalizeCategory(categoryInput.text.toString())
                val body = bodyInput.text.toString().trim()

                if (!validateArticleForm(titleInput, bodyInput, title, body)) {
                    return@setOnClickListener
                }

                saveArticle(article, title, category, body)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showCommentDialog(article: BlogArticle) {
        val authorInput = createDialogInput(getString(R.string.comment_author_hint))
        val bodyInput = createDialogInput(getString(R.string.comment_body_hint), multiline = true)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(authorInput)
            addView(bodyInput)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_comment)
            .setView(form)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.add, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val body = bodyInput.text.toString().trim()
                bodyInput.error = null
                if (body.isEmpty()) {
                    bodyInput.error = getString(R.string.required_field)
                    return@setOnClickListener
                }

                article.comments.add(
                    BlogComment(
                        id = repository.nextCommentId(article),
                        author = authorInput.text.toString().trim()
                            .ifEmpty { getString(R.string.anonymous) },
                        body = body,
                        createdAt = System.currentTimeMillis()
                    )
                )
                article.updatedAt = System.currentTimeMillis()

                persistAndRender()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmDelete(article: BlogArticle) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_article_title)
            .setMessage(R.string.delete_article_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                articles.removeAll { it.id == article.id }
                persistAndRender()
            }
            .show()
    }

    private fun saveArticle(
        article: BlogArticle?,
        title: String,
        category: String,
        body: String
    ) {
        val now = System.currentTimeMillis()
        if (article == null) {
            articles.add(
                BlogArticle(
                    id = repository.nextArticleId(articles),
                    title = title,
                    category = category,
                    body = body,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            article.title = title
            article.category = category
            article.body = body
            article.updatedAt = now
        }

        activeCategory = category
        persistAndRender()
    }

    private fun validateArticleForm(
        titleInput: EditText,
        bodyInput: EditText,
        title: String,
        body: String
    ): Boolean {
        titleInput.error = null
        bodyInput.error = null
        if (title.isEmpty()) {
            titleInput.error = getString(R.string.required_field)
        }
        if (body.isEmpty()) {
            bodyInput.error = getString(R.string.required_field)
        }
        return title.isNotEmpty() && body.isNotEmpty()
    }

    private fun createDialogInput(hintText: String, multiline: Boolean = false): EditText {
        return EditText(this).apply {
            hint = hintText
            inputType = if (multiline) {
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
            isSingleLine = !multiline
            minLines = if (multiline) 4 else 1
            maxLines = if (multiline) 8 else 1
            setTextColor(color(R.color.text_primary))
            setHintTextColor(color(R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }
    }

    private fun availableCategories(): List<String> {
        val categories = articles
            .map { it.category }
            .filterNot { it.equals(allCategoriesLabel(), ignoreCase = true) }
            .distinct()
            .sortedBy { it.lowercase(Locale.getDefault()) }
        return listOf(allCategoriesLabel()) + categories
    }

    private fun persistAndRender() {
        repository.saveArticles(articles)
        renderBlog()
    }

    private fun allCategoriesLabel(): String {
        return getString(R.string.all_categories)
    }

    private fun color(resId: Int): Int {
        return ContextCompat.getColor(this, resId)
    }

    private fun tint(resId: Int): ColorStateList {
        return ColorStateList.valueOf(color(resId))
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
