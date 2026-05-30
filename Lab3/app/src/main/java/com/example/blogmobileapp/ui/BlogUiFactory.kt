package com.example.blogmobileapp.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.blogmobileapp.R
import com.example.blogmobileapp.model.BlogArticle
import com.example.blogmobileapp.model.BlogComment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date

class BlogUiFactory(
    private val context: Context,
    private val dateFormat: SimpleDateFormat
) {
    fun createArticleCard(
        article: BlogArticle,
        onComment: (BlogArticle) -> Unit,
        onEdit: (BlogArticle) -> Unit,
        onDelete: (BlogArticle) -> Unit
    ): MaterialCardView {
        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
            radius = dp(8).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(color(R.color.surface_card))
            setStrokeColor(color(R.color.card_stroke))
            strokeWidth = dp(1)
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        content.addView(
            TextView(context).apply {
                text = article.category
                setTextColor(color(R.color.category_text))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dp(10), dp(4), dp(10), dp(4))
                background = roundedBackground(R.color.category_background, 32)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            TextView(context).apply {
                text = article.title
                setTextColor(color(R.color.text_primary))
                textSize = 21f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, dp(10), 0, 0)
            }
        )

        content.addView(
            TextView(context).apply {
                text = articleMeta(article)
                setTextColor(color(R.color.text_secondary))
                textSize = 12f
                setPadding(0, dp(4), 0, 0)
            }
        )

        content.addView(
            TextView(context).apply {
                text = article.body
                setTextColor(color(R.color.text_primary))
                textSize = 15f
                setLineSpacing(dp(2).toFloat(), 1.0f)
                setPadding(0, dp(12), 0, 0)
            }
        )

        content.addView(
            createActionButton(R.string.comment, R.drawable.ic_comment_24).apply {
                setOnClickListener { onComment(article) }
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        content.addView(createActionRow(article, onEdit, onDelete))
        content.addView(createCommentsSection(article))
        card.addView(content)
        return card
    }

    private fun createActionRow(
        article: BlogArticle,
        onEdit: (BlogArticle) -> Unit,
        onDelete: (BlogArticle) -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END

            addView(
                createActionButton(R.string.edit, R.drawable.ic_edit_24).apply {
                    setOnClickListener { onEdit(article) }
                },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    rightMargin = dp(8)
                }
            )
            addView(
                createActionButton(R.string.delete, R.drawable.ic_delete_24, danger = true).apply {
                    setOnClickListener { onDelete(article) }
                },
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
        }
    }

    private fun createCommentsSection(article: BlogArticle): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)

            addView(
                TextView(context).apply {
                    text = "${context.getString(R.string.comments)} (${article.comments.size})"
                    setTextColor(color(R.color.text_primary))
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                }
            )

            if (article.comments.isEmpty()) {
                addView(
                    TextView(context).apply {
                        text = context.getString(R.string.no_comments)
                        setTextColor(color(R.color.text_secondary))
                        textSize = 13f
                        setPadding(0, dp(6), 0, 0)
                    }
                )
            } else {
                article.comments.sortedBy { it.createdAt }.forEach { comment ->
                    addView(createCommentView(comment))
                }
            }
        }
    }

    private fun createCommentView(comment: BlogComment): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBackground(R.color.comment_background, 8)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }

            addView(
                TextView(context).apply {
                    text = "${comment.author} | ${formatDate(comment.createdAt)}"
                    setTextColor(color(R.color.text_secondary))
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                }
            )
            addView(
                TextView(context).apply {
                    text = comment.body
                    setTextColor(color(R.color.text_primary))
                    textSize = 14f
                    setPadding(0, dp(4), 0, 0)
                }
            )
        }
    }

    private fun createActionButton(
        textRes: Int,
        iconRes: Int,
        danger: Boolean = false
    ): MaterialButton {
        val actionColor = color(if (danger) R.color.danger else R.color.brand_primary)
        return MaterialButton(
            context,
            null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = context.getString(textRes)
            isAllCaps = false
            minHeight = dp(42)
            insetTop = 0
            insetBottom = 0
            iconPadding = dp(6)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            setIconResource(iconRes)
            iconTint = ColorStateList.valueOf(actionColor)
            setTextColor(actionColor)
            strokeColor = ColorStateList.valueOf(actionColor)
            strokeWidth = dp(1)
            cornerRadius = dp(8)
            backgroundTintList = tint(R.color.surface_card)
        }
    }

    private fun articleMeta(article: BlogArticle): String {
        val created = "Створено: ${formatDate(article.createdAt)}"
        return if (article.updatedAt > article.createdAt) {
            "$created | Оновлено: ${formatDate(article.updatedAt)}"
        } else {
            created
        }
    }

    private fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    private fun roundedBackground(colorRes: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color(colorRes))
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun color(resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    private fun tint(resId: Int): ColorStateList {
        return ColorStateList.valueOf(color(resId))
    }

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
