const eyeOpen = '<i class="fa-solid fa-eye" style="font-size:16px;color:#aaa;"></i>';
const eyeClosed = '<i class="fa-solid fa-eye-slash" style="font-size:16px;color:#aaa;"></i>';

function togglePassword() {
  const input = document.getElementById('password');
  const btn = document.getElementById('toggle-btn');
  if (input.type === 'password') {
    input.type = 'text';
    btn.innerHTML = eyeOpen;
  } else {
    input.type = 'password';
    btn.innerHTML = eyeClosed;
  }
}

window.onload = function() {
  document.getElementById('toggle-btn').innerHTML = eyeClosed;

  document.getElementById('username').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') document.getElementById('password').focus();
  });

  document.getElementById('password').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') handleLogin();
  });

  function showError(message) {
    const el = document.getElementById('error-message');
    el.textContent = message;
    el.classList.add('show');
  }

  function hideError() {
    const el = document.getElementById('error-message');
    el.classList.remove('show');
  }

  function handleLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const btn = document.getElementById('login-btn');

    hideError();

    if (!username || !password) {
      showError('帳號或密碼不得為空');
      return;
    }

    btn.disabled = true;
    btn.textContent = '登入中...';

    fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username, password: password })
    })
    .then(function(res) {
      if (res.status === 401) throw new Error('401');
      if (!res.ok) throw new Error('error');
      return res.json();
    })
    .then(function(data) {
      sessionStorage.setItem('userId', data.userId);
      sessionStorage.setItem('username', data.username);
      sessionStorage.setItem('role', data.role);
      window.location.href = 'index.html';
    })
    .catch(function(err) {
      btn.disabled = false;
      btn.textContent = '登 入';
      if (err.message === '401') {
        showError('帳號或密碼錯誤');
      } else {
        showError('系統發生錯誤，請稍後再試');
      }
    });
  }

  document.getElementById('login-btn').onclick = handleLogin;
};