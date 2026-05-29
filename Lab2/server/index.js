const crypto = require("crypto");
const path = require("path");
const cors = require("cors");
const express = require("express");
const { readBlog, writeBlog } = require("./storage");

const app = express();
const PORT = Number(process.env.PORT) || 3000;
const DIST_DIR = path.join(__dirname, "..", "dist");

app.use(
  cors({
    origin: [/^http:\/\/localhost:\d+$/, /^http:\/\/127\.0\.0\.1:\d+$/]
  })
);
app.use(express.json({ limit: "1mb" }));

function normalize(value) {
  return String(value ?? "").trim();
}

function createHttpError(status, message) {
  const error = new Error(message);
  error.status = status;
  return error;
}

function withCategory(article, categories) {
  const category = categories.find((item) => item.id === article.categoryId);
  return {
    ...article,
    category: category
      ? {
          id: category.id,
          name: category.name,
          color: category.color
        }
      : null
  };
}

function sortByNewest(items) {
  return [...items].sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt));
}

app.get("/api/health", (req, res) => {
  res.json({ status: "ok" });
});

app.get("/api/categories", async (req, res, next) => {
  try {
    const data = await readBlog();
    res.json(data.categories);
  } catch (error) {
    next(error);
  }
});

app.post("/api/categories", async (req, res, next) => {
  try {
    const name = normalize(req.body.name);

    if (name.length < 2) {
      throw createHttpError(400, "Назва категорії має містити щонайменше 2 символи.");
    }

    const data = await readBlog();
    const exists = data.categories.some((category) => category.name.toLowerCase() === name.toLowerCase());

    if (exists) {
      throw createHttpError(409, "Така категорія вже існує.");
    }

    const colors = ["#2f8f5b", "#2f78c4", "#d86c45", "#8a6f2a", "#c74d5d", "#4a8f91"];
    const category = {
      id: crypto.randomUUID(),
      name,
      color: colors[data.categories.length % colors.length],
      createdAt: new Date().toISOString()
    };

    data.categories.push(category);
    await writeBlog(data);
    res.status(201).json(category);
  } catch (error) {
    next(error);
  }
});

app.get("/api/articles", async (req, res, next) => {
  try {
    const data = await readBlog();
    const categoryId = normalize(req.query.categoryId);
    const articles =
      categoryId && categoryId !== "all"
        ? data.articles.filter((article) => article.categoryId === categoryId)
        : data.articles;

    res.json(sortByNewest(articles).map((article) => withCategory(article, data.categories)));
  } catch (error) {
    next(error);
  }
});

app.get("/api/articles/:id", async (req, res, next) => {
  try {
    const data = await readBlog();
    const article = data.articles.find((item) => item.id === req.params.id);

    if (!article) {
      throw createHttpError(404, "Статтю не знайдено.");
    }

    res.json(withCategory(article, data.categories));
  } catch (error) {
    next(error);
  }
});

app.post("/api/articles", async (req, res, next) => {
  try {
    const title = normalize(req.body.title);
    const author = normalize(req.body.author) || "Анонім";
    const excerpt = normalize(req.body.excerpt);
    const content = normalize(req.body.content);
    const categoryId = normalize(req.body.categoryId);

    if (title.length < 3) {
      throw createHttpError(400, "Заголовок має містити щонайменше 3 символи.");
    }

    if (content.length < 10) {
      throw createHttpError(400, "Текст статті має містити щонайменше 10 символів.");
    }

    const data = await readBlog();
    const category = data.categories.find((item) => item.id === categoryId);

    if (!category) {
      throw createHttpError(400, "Оберіть наявну категорію.");
    }

    const article = {
      id: crypto.randomUUID(),
      title,
      author,
      excerpt: excerpt || `${content.slice(0, 140)}${content.length > 140 ? "..." : ""}`,
      content,
      categoryId,
      createdAt: new Date().toISOString(),
      comments: []
    };

    data.articles.push(article);
    await writeBlog(data);
    res.status(201).json(withCategory(article, data.categories));
  } catch (error) {
    next(error);
  }
});

app.post("/api/articles/:id/comments", async (req, res, next) => {
  try {
    const author = normalize(req.body.author) || "Гість";
    const text = normalize(req.body.text);

    if (text.length < 2) {
      throw createHttpError(400, "Коментар має містити щонайменше 2 символи.");
    }

    const data = await readBlog();
    const article = data.articles.find((item) => item.id === req.params.id);

    if (!article) {
      throw createHttpError(404, "Статтю не знайдено.");
    }

    const comment = {
      id: crypto.randomUUID(),
      author,
      text,
      createdAt: new Date().toISOString()
    };

    article.comments = Array.isArray(article.comments) ? article.comments : [];
    article.comments.push(comment);
    await writeBlog(data);
    res.status(201).json(withCategory(article, data.categories));
  } catch (error) {
    next(error);
  }
});

app.use(express.static(DIST_DIR));

app.get("*", (req, res) => {
  res.sendFile(path.join(DIST_DIR, "index.html"), (error) => {
    if (error) {
      res.status(404).json({
        message: "React-збірку не знайдено. Запустіть npm run build або npm run dev:client."
      });
    }
  });
});

app.use((error, req, res, next) => {
  const status = error.status || 500;
  res.status(status).json({
    message: error.message || "Сталася помилка сервера."
  });
});

app.listen(PORT, () => {
  console.log(`Express API started on http://127.0.0.1:${PORT}`);
});
