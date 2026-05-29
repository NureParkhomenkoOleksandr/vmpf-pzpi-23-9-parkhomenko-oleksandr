import { useEffect, useMemo, useState } from "react";
import {
  BookOpen,
  CalendarDays,
  FolderPlus,
  MessageCircle,
  PenLine,
  PlusCircle,
  RefreshCw,
  Send,
  Tags
} from "lucide-react";

const API_URL = "/api";

const emptyArticle = {
  title: "",
  author: "",
  excerpt: "",
  content: "",
  categoryId: ""
};

const emptyComment = {
  author: "",
  text: ""
};

function formatDate(value) {
  return new Intl.DateTimeFormat("uk-UA", {
    day: "2-digit",
    month: "long",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

async function requestJson(url, options) {
  const response = await fetch(url, options);
  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(payload.message || "Запит не виконано.");
  }

  return payload;
}

function App() {
  const [articles, setArticles] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [activeArticleId, setActiveArticleId] = useState("");
  const [articleForm, setArticleForm] = useState(emptyArticle);
  const [categoryName, setCategoryName] = useState("");
  const [commentForm, setCommentForm] = useState(emptyComment);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmittingArticle, setIsSubmittingArticle] = useState(false);
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    setIsLoading(true);
    setError("");

    try {
      const [loadedCategories, loadedArticles] = await Promise.all([
        requestJson(`${API_URL}/categories`),
        requestJson(`${API_URL}/articles`)
      ]);

      setCategories(loadedCategories);
      setArticles(loadedArticles);
      setArticleForm((current) => ({
        ...current,
        categoryId: current.categoryId || loadedCategories[0]?.id || ""
      }));
      setActiveArticleId((current) => current || loadedArticles[0]?.id || "");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const visibleArticles = useMemo(() => {
    if (selectedCategory === "all") {
      return articles;
    }

    return articles.filter((article) => article.categoryId === selectedCategory);
  }, [articles, selectedCategory]);

  const categoryCounts = useMemo(() => {
    return articles.reduce(
      (counts, article) => ({
        ...counts,
        [article.categoryId]: (counts[article.categoryId] || 0) + 1
      }),
      {}
    );
  }, [articles]);

  const activeArticle = useMemo(() => {
    return articles.find((article) => article.id === activeArticleId) || visibleArticles[0] || null;
  }, [articles, activeArticleId, visibleArticles]);

  function updateArticleForm(event) {
    const { name, value } = event.target;
    setArticleForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  function updateCommentForm(event) {
    const { name, value } = event.target;
    setCommentForm((current) => ({
      ...current,
      [name]: value
    }));
  }

  async function submitArticle(event) {
    event.preventDefault();
    setIsSubmittingArticle(true);
    setError("");
    setNotice("");

    try {
      const createdArticle = await requestJson(`${API_URL}/articles`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(articleForm)
      });

      setArticles((current) => [createdArticle, ...current]);
      setArticleForm({
        ...emptyArticle,
        categoryId: createdArticle.categoryId
      });
      setSelectedCategory("all");
      setActiveArticleId(createdArticle.id);
      setNotice("Статтю додано.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsSubmittingArticle(false);
    }
  }

  async function submitCategory(event) {
    event.preventDefault();
    setError("");
    setNotice("");

    try {
      const createdCategory = await requestJson(`${API_URL}/categories`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ name: categoryName })
      });

      setCategories((current) => [...current, createdCategory]);
      setArticleForm((current) => ({
        ...current,
        categoryId: current.categoryId || createdCategory.id
      }));
      setCategoryName("");
      setNotice("Категорію додано.");
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function submitComment(event) {
    event.preventDefault();

    if (!activeArticle) {
      return;
    }

    setIsSubmittingComment(true);
    setError("");
    setNotice("");

    try {
      const updatedArticle = await requestJson(`${API_URL}/articles/${activeArticle.id}/comments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(commentForm)
      });

      setArticles((current) =>
        current.map((article) => (article.id === updatedArticle.id ? updatedArticle : article))
      );
      setCommentForm(emptyComment);
      setNotice("Коментар додано.");
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsSubmittingComment(false);
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">
            <BookOpen size={23} aria-hidden="true" />
          </span>
          <div>
            <h1>BlogLab</h1>
            <p>Express API + React SPA</p>
          </div>
        </div>
        <button className="ghost-button" type="button" onClick={loadData} disabled={isLoading}>
          <RefreshCw size={18} aria-hidden="true" />
          Оновити
        </button>
      </header>

      <main className="layout">
        <aside className="sidebar">
          <section className="panel">
            <div className="section-title">
              <Tags size={18} aria-hidden="true" />
              <h2>Категорії</h2>
            </div>
            <div className="category-list">
              <button
                className={selectedCategory === "all" ? "category-pill active" : "category-pill"}
                type="button"
                onClick={() => setSelectedCategory("all")}
              >
                <span className="swatch all" />
                Усі
                <strong>{articles.length}</strong>
              </button>

              {categories.map((category) => (
                <button
                  className={selectedCategory === category.id ? "category-pill active" : "category-pill"}
                  type="button"
                  key={category.id}
                  onClick={() => {
                    setSelectedCategory(category.id);
                    const nextArticle = articles.find((article) => article.categoryId === category.id);
                    setActiveArticleId(nextArticle?.id || "");
                  }}
                >
                  <span className="swatch" style={{ backgroundColor: category.color }} />
                  {category.name}
                  <strong>{categoryCounts[category.id] || 0}</strong>
                </button>
              ))}
            </div>
          </section>

          <section className="panel">
            <div className="section-title">
              <FolderPlus size={18} aria-hidden="true" />
              <h2>Нова категорія</h2>
            </div>
            <form className="compact-form" onSubmit={submitCategory}>
              <label>
                Назва
                <input
                  name="categoryName"
                  value={categoryName}
                  onChange={(event) => setCategoryName(event.target.value)}
                  placeholder="Наприклад, Backend"
                />
              </label>
              <button className="primary-button" type="submit">
                <PlusCircle size={18} aria-hidden="true" />
                Додати
              </button>
            </form>
          </section>
        </aside>

        <section className="content-column">
          <div className="status-row" aria-live="polite">
            {isLoading && <span className="info">Завантаження...</span>}
            {notice && <span className="success">{notice}</span>}
            {error && <span className="error">{error}</span>}
          </div>

          <section className="panel article-form-panel">
            <div className="section-title">
              <PenLine size={18} aria-hidden="true" />
              <h2>Нова стаття</h2>
            </div>
            <form className="article-form" onSubmit={submitArticle}>
              <div className="form-grid">
                <label>
                  Заголовок
                  <input name="title" value={articleForm.title} onChange={updateArticleForm} />
                </label>
                <label>
                  Автор
                  <input name="author" value={articleForm.author} onChange={updateArticleForm} />
                </label>
                <label>
                  Категорія
                  <select name="categoryId" value={articleForm.categoryId} onChange={updateArticleForm}>
                    {categories.map((category) => (
                      <option value={category.id} key={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              <label>
                Короткий опис
                <input name="excerpt" value={articleForm.excerpt} onChange={updateArticleForm} />
              </label>
              <label>
                Текст
                <textarea name="content" value={articleForm.content} onChange={updateArticleForm} rows={5} />
              </label>
              <button className="primary-button wide-button" type="submit" disabled={isSubmittingArticle}>
                <PlusCircle size={18} aria-hidden="true" />
                {isSubmittingArticle ? "Додавання..." : "Додати статтю"}
              </button>
            </form>
          </section>

          <div className="workspace-grid">
            <section className="article-list">
              <div className="section-title inline-title">
                <BookOpen size={18} aria-hidden="true" />
                <h2>Статті</h2>
              </div>
              {visibleArticles.length === 0 && !isLoading ? (
                <p className="empty-state">Немає статей у цій категорії.</p>
              ) : (
                <div className="article-stack">
                  {visibleArticles.map((article) => (
                    <button
                      className={activeArticle?.id === article.id ? "article-item active" : "article-item"}
                      type="button"
                      key={article.id}
                      onClick={() => setActiveArticleId(article.id)}
                    >
                      <span className="article-category">
                        <span className="swatch" style={{ backgroundColor: article.category?.color || "#69707a" }} />
                        {article.category?.name || "Без категорії"}
                      </span>
                      <strong>{article.title}</strong>
                      <span>{article.excerpt}</span>
                      <small>
                        <MessageCircle size={14} aria-hidden="true" />
                        {article.comments?.length || 0}
                      </small>
                    </button>
                  ))}
                </div>
              )}
            </section>

            <section className="article-detail">
              {activeArticle ? (
                <>
                  <div className="article-meta">
                    <span className="article-category">
                      <span
                        className="swatch"
                        style={{ backgroundColor: activeArticle.category?.color || "#69707a" }}
                      />
                      {activeArticle.category?.name || "Без категорії"}
                    </span>
                    <span>
                      <CalendarDays size={15} aria-hidden="true" />
                      {formatDate(activeArticle.createdAt)}
                    </span>
                  </div>
                  <article>
                    <h2>{activeArticle.title}</h2>
                    <p className="byline">Автор: {activeArticle.author}</p>
                    <p className="lead">{activeArticle.excerpt}</p>
                    <p>{activeArticle.content}</p>
                  </article>

                  <div className="comments-block">
                    <div className="section-title inline-title">
                      <MessageCircle size={18} aria-hidden="true" />
                      <h3>Коментарі</h3>
                    </div>
                    <div className="comments-list">
                      {activeArticle.comments?.length ? (
                        activeArticle.comments.map((comment) => (
                          <div className="comment" key={comment.id}>
                            <div>
                              <strong>{comment.author}</strong>
                              <span>{formatDate(comment.createdAt)}</span>
                            </div>
                            <p>{comment.text}</p>
                          </div>
                        ))
                      ) : (
                        <p className="empty-state">Коментарів поки немає.</p>
                      )}
                    </div>

                    <form className="comment-form" onSubmit={submitComment}>
                      <label>
                        Ім'я
                        <input name="author" value={commentForm.author} onChange={updateCommentForm} />
                      </label>
                      <label>
                        Коментар
                        <textarea name="text" value={commentForm.text} onChange={updateCommentForm} rows={3} />
                      </label>
                      <button className="primary-button" type="submit" disabled={isSubmittingComment}>
                        <Send size={18} aria-hidden="true" />
                        {isSubmittingComment ? "Надсилання..." : "Надіслати"}
                      </button>
                    </form>
                  </div>
                </>
              ) : (
                <p className="empty-state">Оберіть або додайте статтю.</p>
              )}
            </section>
          </div>
        </section>
      </main>
    </div>
  );
}

export default App;
