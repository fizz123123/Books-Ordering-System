let currentRole = '';
let editingBookId = null;
let currentView = 'list';
let currentPage = 1;       // 後端頁碼從 1 開始
let totalPages = 1;
let currentKeyword = '';

function switchView(view) {
  currentView = view;
  document.getElementById('btn-grid').classList.toggle('active', view === 'grid');
  document.getElementById('btn-list').classList.toggle('active', view === 'list');
  document.getElementById('book-grid').style.display = view === 'grid' ? 'grid' : 'none';
  document.getElementById('book-list').style.display = view === 'list' ? 'block' : 'none';
}

// 頁面載入時初始化
window.onload = function() {
  // 預設切換到清單模式
  document.getElementById('btn-grid').classList.remove('active');
  document.getElementById('btn-list').classList.add('active');

  let role = sessionStorage.getItem('role');
  let username = sessionStorage.getItem('username');

  // 未登入則跳回登入頁
  if (!role) {
    window.location.href = 'login.html';
    return;
  }

  currentRole = role;
  document.getElementById('header-username').textContent = username;
  document.getElementById('dropdown-username').textContent = username;

  // 根據角色切換圖示
  const adminSVG = `<i id="user-icon" class="fa-solid fa-user-shield" style="font-size:15px;color:#F5C9C9;"></i>`;
  const userSVG = `<i id="user-icon" class="fa-solid fa-user" style="font-size:15px;color:#F5C9C9;"></i>`;
  document.getElementById('user-icon').outerHTML = role === 'ADMIN' ? adminSVG : userSVG;

  // ADMIN 才顯示新增按鈕
  if (role === 'ADMIN') {
    document.getElementById('add-btn').style.display = 'block';
  }

  loadBooks();
};

// 載入書籍列表（配合後端分頁）
async function loadBooks(keyword, page) {
  if (keyword !== undefined) currentKeyword = keyword;
  if (page !== undefined) currentPage = page;

  try {
    const data = await bookService.getBooks(currentPage, currentKeyword);
    totalPages = data.totalPages || 1;
    renderBooks(data.content || []);
    renderPagination();
  } catch (error) {
    renderBooks([]);
  }
}

// 渲染分頁按鈕（從後端 totalPages 決定）
function renderPagination() {
  const pagination = document.getElementById('pagination');
  pagination.innerHTML = '';
  if (totalPages <= 1) return;

  // 上一頁
  const prev = document.createElement('button');
  prev.className = 'page-btn';
  prev.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
  prev.disabled = currentPage === 1;
  prev.onclick = function() { loadBooks(currentKeyword, currentPage - 1); };
  pagination.appendChild(prev);

  // 頁碼
  const pages = [];
  if (totalPages <= 7) {
    for (let i = 1; i <= totalPages; i++) pages.push(i);
  } else {
    pages.push(1);
    if (currentPage > 4) pages.push('...');
    for (let i = Math.max(2, currentPage - 1); i <= Math.min(totalPages - 1, currentPage + 1); i++) pages.push(i);
    if (currentPage < totalPages - 3) pages.push('...');
    pages.push(totalPages);
  }

  pages.forEach(function(p) {
    const btn = document.createElement('button');
    if (p === '...') {
      btn.className = 'page-btn dots';
      btn.textContent = '...';
    } else {
      btn.className = 'page-btn' + (p === currentPage ? ' active' : '');
      btn.textContent = p;
      btn.onclick = function() { loadBooks(currentKeyword, p); };
    }
    pagination.appendChild(btn);
  });

  // 下一頁
  const next = document.createElement('button');
  next.className = 'page-btn';
  next.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
  next.disabled = currentPage >= totalPages;
  next.onclick = function() { loadBooks(currentKeyword, currentPage + 1); };
  pagination.appendChild(next);
}

// 渲染書籍（後端已分頁，直接渲染全部 content）
function renderBooks(books) {
  const grid = document.getElementById('book-grid');
  const empty = document.getElementById('empty-state');
  const tbody = document.getElementById('book-table-body');
  grid.innerHTML = '';
  tbody.innerHTML = '';

  // 依目前視圖模式切換顯示
  grid.style.display = currentView === 'grid' ? 'grid' : 'none';
  document.getElementById('book-list').style.display = currentView === 'list' ? 'block' : 'none';

  if (books.length === 0) {
    empty.textContent = currentKeyword ? '查無符合條件的書籍' : '目前書庫尚未上架任何書籍';
    empty.style.display = 'block';
    document.getElementById('pagination').innerHTML = '';
    return;
  }

  empty.style.display = 'none';

  // 清單模式渲染
  const thActions = document.getElementById('th-actions');
  thActions.style.display = '';
  thActions.textContent = currentRole === 'ADMIN' ? '操作' : '連結';
  books.forEach(function(book) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${book.title}</td>
      <td>${book.author}</td>
      <td>${book.publisher}</td>
      <td>${book.isbn}</td>
      <td>NT$ ${book.price}</td>
      <td><div class="td-actions">
        <button class="link-btn" onclick="${book.publisherBookUrl ? `window.open('${book.publisherBookUrl}', '_blank')` : `alert('此書籍尚無連結')`}" title="開啟連結">
          <i class="fa-solid fa-arrow-up-right-from-square"></i>
        </button>
        ${currentRole === 'ADMIN' ? `
        <button class="edit-btn" onclick="openEditModal(${book.bookId})" title="修改">
          <i class="fa-solid fa-pen-to-square"></i>
        </button>
        <button class="delete-btn" onclick="handleDelete(${book.bookId})" title="刪除">
          <i class="fa-solid fa-trash-can"></i>
        </button>` : ''}
      </div></td>
    `;
    tbody.appendChild(tr);
  });

  // 卡片模式渲染
  books.forEach(function(book) {
    const card = document.createElement('div');
    card.className = 'book-card';
    card.innerHTML = `
      <div class="book-content">
        <div style="display:flex;flex-direction:column;gap:6px;">
            <div class="book-title" title="${book.title}"
              style="${book.publisherBookUrl ? 'cursor:pointer;text-decoration:underline;' : 'cursor:default;'}"
              onclick="${book.publisherBookUrl ? `window.open('${book.publisherBookUrl}', '_blank')` : `alert('此書籍尚無連結')`}"
            >${book.title}</div>
            <div class="book-author">${book.author}</div>
            <div class="book-info">
              出版社：${book.publisher}<br>
              ISBN：${book.isbn}<br>
              建立時間：${formatDate(book.createdAt)}<br>
              更新時間：${formatDate(book.updatedAt)}
            </div>
            <div class="book-price">NT$ ${book.price}</div>
          </div>
        </div>
        ${currentRole === 'ADMIN' ? `
        <div class="book-actions-hover">
          <button class="edit-btn" onclick="openEditModal(${book.bookId})" title="修改">
            <i class="fa-solid fa-pen-to-square"></i>
          </button>
          <button class="delete-btn" onclick="handleDelete(${book.bookId})" title="刪除">
            <i class="fa-solid fa-trash-can"></i>
          </button>
        </div>` : ''}
      </div>
    `;
    grid.appendChild(card);
  });
}

// 日期格式化
function formatDate(dateStr) {
  if (!dateStr) return '-';
  return dateStr.replace('T', ' ').substring(0, 16);
}

// 下拉選單開關
function toggleDropdown() {
  const menu = document.getElementById('dropdown-menu');
  const toggle = document.getElementById('user-toggle');
  menu.classList.toggle('show');
  toggle.classList.toggle('open');
}

// 點擊其他地方關閉選單
document.addEventListener('click', function(e) {
  const dropdown = document.querySelector('.user-dropdown');
  if (dropdown && !dropdown.contains(e.target)) {
    document.getElementById('dropdown-menu').classList.remove('show');
    document.getElementById('user-toggle').classList.remove('open');
  }
});

// 輸入時控制 ✕ 顯示
function handleSearchInput() {
  const val = document.getElementById('search-input').value;
  const btn = document.getElementById('clear-btn');
  btn.style.display = val ? 'flex' : 'none';
}

// 清除搜尋
function clearSearch() {
  document.getElementById('search-input').value = '';
  document.getElementById('clear-btn').style.display = 'none';
  currentKeyword = '';
  loadBooks('', 1);
}

// 搜尋
function handleSearch() {
  const keyword = document.getElementById('search-input').value.trim();
  loadBooks(keyword, 1);
}

// 按 Enter 搜尋（只在搜尋欄位內生效，避免在其他地方誤觸跳回第1頁）
document.getElementById('search-input').addEventListener('keydown', function(e) {
  if (e.key === 'Enter') handleSearch();
});

// 開啟新增 Modal
function openAddModal() {
  editingBookId = null;
  document.getElementById('modal-title').textContent = '新增書籍';
  clearForm();
  document.getElementById('modal-overlay').classList.add('show');
}

// 開啟修改 Modal
async function openEditModal(bookId) {
  editingBookId = bookId;
  document.getElementById('modal-title').textContent = '修改書籍';
  clearForm();

  try {
    const book = await bookService.getBookById(bookId);
    document.getElementById('f-title').value = book.title;
    document.getElementById('f-author').value = book.author;
    document.getElementById('f-publisher').value = book.publisher;
    document.getElementById('f-isbn').value = book.isbn;
    document.getElementById('f-price').value = book.price;
    document.getElementById('f-url').value = book.publisherBookUrl || '';
    document.getElementById('modal-overlay').classList.add('show');
  } catch (error) {
    alert('無法取得書籍資料');
  }
}

// 關閉 Modal
function closeModal() {
  document.getElementById('modal-overlay').classList.remove('show');
  clearForm();
}

// 清空表單
function clearForm() {
  ['f-title','f-author','f-publisher','f-isbn','f-price','f-url'].forEach(function(id) {
    document.getElementById(id).value = '';
  });
  document.getElementById('modal-error').classList.remove('show');
}

// 表單送出（新增或修改）
async function handleSubmit() {
  const title          = document.getElementById('f-title').value.trim();
  const author         = document.getElementById('f-author').value.trim();
  const publisher      = document.getElementById('f-publisher').value.trim();
  const isbn           = document.getElementById('f-isbn').value.trim();
  const price          = document.getElementById('f-price').value;
  const publisherBookUrl = document.getElementById('f-url').value.trim();

  if (!title)     return showModalError('書名不可為空');
  if (!author)    return showModalError('作者不可為空');
  if (!publisher) return showModalError('出版社不可為空');
  if (!isbn)      return showModalError('ISBN 不可為空');
  if (price === '' || Number(price) < 0) return showModalError('價格不可小於 0');

  const body = { title, author, publisher, isbn, price: Number(price), publisherBookUrl };
  const isCreating = !editingBookId;

  try {
    if (editingBookId) {
      await bookService.updateBook(editingBookId, body);
    } else {
      await bookService.createBook(body);
    }
    closeModal();
    // 新增成功後跳回第 1 頁，才能看到新書（依 bookId 新到舊排序）
    loadBooks(currentKeyword, isCreating ? 1 : currentPage);
  } catch (error) {
    showModalError('系統發生錯誤，請稍後再試');
  }
}

let deletingBookId = null;

// 開啟刪除確認視窗
function handleDelete(bookId) {
  deletingBookId = bookId;
  document.getElementById('confirm-overlay').classList.add('show');
}

// 關閉刪除確認視窗
function closeConfirm() {
  deletingBookId = null;
  document.getElementById('confirm-overlay').classList.remove('show');
}

// 確認刪除
async function confirmDelete() {
  if (!deletingBookId) return;

  try {
    await bookService.deleteBook(deletingBookId);
    closeConfirm();
    // 若刪除後當頁已無資料，退回上一頁
    loadBooks(currentKeyword, currentPage > 1 ? currentPage - 1 : 1);
  } catch (error) {
    closeConfirm();
    alert('刪除失敗，請稍後再試');
  }
}

// 顯示 Modal 錯誤訊息
function showModalError(msg) {
  const el = document.getElementById('modal-error');
  el.textContent = msg;
  el.classList.add('show');
}

// 登出
async function handleLogout() {
  try {
    await authService.logout();
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    sessionStorage.clear();
    window.location.href = 'login.html';
  }
}
