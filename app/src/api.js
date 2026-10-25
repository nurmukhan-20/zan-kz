export const LAPTOP_IP = '10.250.82.130'; // ← IP вашего ноутбука (ipconfig → IPv4)

export const SERVER_URL = `http://${LAPTOP_IP}:8080`;
export const BASE_URL = `${SERVER_URL}/api`;

// фото и видео новостей: «/news/n3-1.jpg» -> полный адрес на сервере
export const mediaUrl = (path) => (!path ? '' : /^https?:\/\//.test(path) ? path : SERVER_URL + path);
export const mediaList = (value) => String(value || '').split(',').map((x) => x.trim()).filter(Boolean).map(mediaUrl);

async function request(path, { method = 'GET', body, token, timeout = 10000 } = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeout);
  try {
    const response = await fetch(BASE_URL + path, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
      throw new Error((data && data.message) || `HTTP ${response.status}`);
    }
    return data;
  } finally {
    clearTimeout(timer);
  }
}

const q = (value) => encodeURIComponent(value);

export const api = {
  categories: () => request('/categories'),
  laws: (categoryId) => request(categoryId ? `/laws?categoryId=${categoryId}` : '/laws'),
  law: (id) => request(`/laws/${id}`),
  articles: (lawId, section) =>
    request(`/articles?lawId=${lawId}${section ? `&section=${q(section)}` : ''}`),
  article: (id) => request(`/articles/${id}`),
  search: (text) => request(`/articles/search?q=${q(text)}`),
  terms: (text) => request(text ? `/terms?q=${q(text)}` : '/terms'),
  notifications: () => request('/notifications'),
  news: (sort = 'latest') => request(`/news?sort=${q(sort)}`),
  newsItem: (id) => request(`/news/${id}`),
  popularArticles: (limit = 10) => request(`/articles/popular?limit=${limit}`),

  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password } }),
  register: (name, email, password) =>
    request('/auth/register', { method: 'POST', body: { name, email, password } }),

  favorites: (token) => request('/favorites', { token }),
  addFavorite: (token, articleId) => request(`/favorites/${articleId}`, { method: 'POST', token }),
  removeFavorite: (token, articleId) => request(`/favorites/${articleId}`, { method: 'DELETE', token }),
  history: (token) => request('/history', { token }),
  addHistory: (token, articleId) => request(`/history/${articleId}`, { method: 'POST', token }),
  clearHistory: (token) => request('/history', { method: 'DELETE', token }),

  // ИИ-помощник: ждём до 45 секунд, ИИ думает дольше обычного запроса
  ask: (token, question, lang) =>
    request('/assistant/ask', { method: 'POST', token, body: { question, lang }, timeout: 45000 }),
};