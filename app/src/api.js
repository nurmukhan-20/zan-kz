export const LAPTOP_IP = '10.255.136.130';

export const BASE_URL = `http://${LAPTOP_IP}:8080/api`;

async function request(path, { method = 'GET', body, token } = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 10000);
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

  login: (email, password) => request('/auth/login', { method: 'POST', body: { email, password } }),
  register: (name, email, password) =>
    request('/auth/register', { method: 'POST', body: { name, email, password } }),

  favorites: (token) => request('/favorites', { token }),
  addFavorite: (token, articleId) => request(`/favorites/${articleId}`, { method: 'POST', token }),
  removeFavorite: (token, articleId) => request(`/favorites/${articleId}`, { method: 'DELETE', token }),
  history: (token) => request('/history', { token }),
  addHistory: (token, articleId) => request(`/history/${articleId}`, { method: 'POST', token }),
  clearHistory: (token) => request('/history', { method: 'DELETE', token }),
};