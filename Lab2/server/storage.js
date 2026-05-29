const fs = require("fs/promises");
const path = require("path");

const DATA_FILE = path.join(__dirname, "data", "blog.json");

const initialBlog = {
  categories: [
    {
      id: "cat-node",
      name: "Node.js",
      color: "#2f8f5b"
    },
    {
      id: "cat-react",
      name: "React",
      color: "#2f78c4"
    },
    {
      id: "cat-web",
      name: "Web Design",
      color: "#d86c45"
    }
  ],
  articles: [
    {
      id: "article-express-start",
      title: "Перший сервер на Express",
      author: "Admin",
      excerpt: "Express допомагає швидко створити API, описати маршрути та повернути дані для React-клієнта.",
      content:
        "Express - це мінімалістичний фреймворк для Node.js. У цьому блозі сервер приймає HTTP-запити, читає статті з JSON-файлу та повертає їх у форматі API. Такий підхід добре підходить для навчального проєкту, бо логіка зберігання даних залишається прозорою.",
      categoryId: "cat-node",
      createdAt: "2026-05-29T08:00:00.000Z",
      comments: [
        {
          id: "comment-1",
          author: "Олена",
          text: "Зручно, що API можна перевірити окремо від React.",
          createdAt: "2026-05-29T08:10:00.000Z"
        }
      ]
    },
    {
      id: "article-react-state",
      title: "Стан у React-компонентах",
      author: "Admin",
      excerpt: "React оновлює інтерфейс автоматично, коли змінюється стан списку статей, форм або активної категорії.",
      content:
        "Фронтенд використовує useState для форм, списку статей, коментарів і вибраної категорії. Після додавання нової статті або коментаря застосунок оновлює локальний стан, тому користувач одразу бачить результат без перезавантаження сторінки.",
      categoryId: "cat-react",
      createdAt: "2026-05-28T16:30:00.000Z",
      comments: []
    },
    {
      id: "article-category-system",
      title: "Категорії для статей",
      author: "Admin",
      excerpt: "Категорії допомагають групувати матеріали та швидко фільтрувати список статей.",
      content:
        "Кожна стаття має categoryId, а сервер перевіряє, чи існує така категорія. На клієнті категорії показані як фільтри, тому можна переглянути всі матеріали або тільки публікації з конкретного напряму.",
      categoryId: "cat-web",
      createdAt: "2026-05-27T11:45:00.000Z",
      comments: [
        {
          id: "comment-2",
          author: "Максим",
          text: "Фільтри роблять список набагато зрозумілішим.",
          createdAt: "2026-05-27T12:05:00.000Z"
        }
      ]
    }
  ]
};

async function ensureDataFile() {
  await fs.mkdir(path.dirname(DATA_FILE), { recursive: true });

  try {
    await fs.access(DATA_FILE);
  } catch {
    await writeBlog(initialBlog);
  }
}

async function readBlog() {
  await ensureDataFile();
  const raw = await fs.readFile(DATA_FILE, "utf8");
  const parsed = JSON.parse(raw.replace(/^\uFEFF/, ""));

  return {
    categories: Array.isArray(parsed.categories) ? parsed.categories : [],
    articles: Array.isArray(parsed.articles) ? parsed.articles : []
  };
}

async function writeBlog(data) {
  await fs.mkdir(path.dirname(DATA_FILE), { recursive: true });
  await fs.writeFile(DATA_FILE, `${JSON.stringify(data, null, 2)}\n`, "utf8");
  return data;
}

module.exports = {
  readBlog,
  writeBlog
};
