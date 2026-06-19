/**
 * 統一的請求處理器
 * @param {string} endpoint - API 的端點路徑 (e.g., '/books')
 * @param {object} options - fetch 的設定物件
 * @returns {Promise<any>}
 */
async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  };

  try {
    const response = await fetch(url, config);
    if (!response.ok) {
      // 讓外部的 catch 能接收到錯誤
      throw new Error(`API Error: ${response.status}`);
    }
    // 處理 DELETE 請求等沒有回傳內容的成功回應
    if (response.status === 204 || response.headers.get('Content-Length') === '0') {
      return;
    }
    return response.json();
  } catch (error) {
    console.error(`Request to ${url} failed:`, error);
    // 將錯誤再次拋出，讓呼叫者可以處理
    throw error;
  }
}

// --- 服務封裝 ---

const authService = {
  login: (username, password) => request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  }),
  logout: () => request('/auth/logout', { method: 'POST' }),
};

const bookService = {
  getBooks: (page, keyword) => {
    const params = new URLSearchParams({ page });
    if (keyword) params.append('keyword', keyword);
    return request(`/books?${params.toString()}`);
  },
  getBookById: (id) => request(`/books/${id}`),
  createBook: (bookData) => request('/books', {
    method: 'POST',
    body: JSON.stringify(bookData),
  }),
  updateBook: (id, bookData) => request(`/books/${id}`, {
    method: 'PUT',
    body: JSON.stringify(bookData),
  }),
  deleteBook: (id) => request(`/books/${id}`, { method: 'DELETE' }),
};
